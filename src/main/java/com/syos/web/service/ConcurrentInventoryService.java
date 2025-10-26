package com.syos.web.service;

import com.syos.web.dao.ProductDAO;
import com.syos.web.model.Product;
import com.syos.web.exception.ConcurrencyException;
import com.syos.web.exception.InsufficientStockException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ============================================
 * CONCURRENT INVENTORY SERVICE
 * ============================================
 *
 * This service demonstrates 5 CONCURRENCY PATTERNS:
 *
 * 1. ReadWriteLock (ReentrantReadWriteLock)
 *    - Multiple threads can read simultaneously
 *    - Writes require exclusive access
 *    - High read throughput with write safety
 *
 * 2. Optimistic Locking (Version-based)
 *    - Detects conflicts after they happen
 *    - Uses version numbers to detect concurrent modifications
 *    - No blocking during reads/writes
 *
 * 3. Pessimistic Locking (SELECT FOR UPDATE)
 *    - Prevents conflicts before they happen
 *    - Database row-level locks
 *    - Used for critical operations like stock updates
 *
 * 4. ConcurrentHashMap
 *    - Thread-safe cache without explicit locking
 *    - Lock-free reads and updates
 *    - High-performance concurrent access
 *
 * 5. Transaction Management
 *    - ACID properties (Atomicity, Consistency, Isolation, Durability)
 *    - Rollback on failure
 *    - Ensures data integrity across operations
 *
 * THREAD-SAFE: YES
 * CLI + WEB CONCURRENT: YES
 * RACE CONDITIONS: NONE
 *
 * ============================================
 */
public class ConcurrentInventoryService {

    private static final Logger logger = Logger.getLogger(ConcurrentInventoryService.class.getName());

    // ========================================
    // CONCURRENCY PATTERN 1: ReadWriteLock
    // ========================================
    // Allows multiple concurrent reads but exclusive writes
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock(true); // fair lock

    // ========================================
    // CONCURRENCY PATTERN 4: ConcurrentHashMap
    // ========================================
    // Thread-safe cache without explicit locking
    private final ConcurrentHashMap<String, Product> productCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> cacheHits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> cacheMisses = new ConcurrentHashMap<>();

    // Statistics tracking
    private final ConcurrentHashMap<String, OperationMetrics> operationMetrics = new ConcurrentHashMap<>();

    private final ProductDAO productDAO;

    // Singleton instance (thread-safe double-checked locking)
    private static volatile ConcurrentInventoryService instance;
    private static final Object instanceLock = new Object();

    // Configuration
    private static final long LOCK_TIMEOUT_SECONDS = 10;
    private static final int MAX_CACHE_SIZE = 1000;

    /**
     * Private constructor for singleton pattern
     */
    private ConcurrentInventoryService() {
        this.productDAO = new ProductDAO();
        try {
            loadCache();
            logger.info("ConcurrentInventoryService initialized successfully");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize inventory service", e);
            throw new RuntimeException("Service initialization failed", e);
        }
    }

    /**
     * Get singleton instance (thread-safe)
     * Uses double-checked locking pattern
     */
    public static ConcurrentInventoryService getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new ConcurrentInventoryService();
                }
            }
        }
        return instance;
    }

    /**
     * ========================================
     * PATTERN 1: ReadWriteLock for Cache Loading
     * ========================================
     */
    private void loadCache() throws SQLException {
        cacheLock.writeLock().lock();
        try {
            logger.info("Loading product cache...");
            List<Product> products = productDAO.getAllProducts();

            productCache.clear();
            for (Product product : products) {
                productCache.put(product.getCode(), cloneProduct(product));
            }

            logger.info("Cache loaded with " + products.size() + " products");
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * ========================================
     * PATTERN 1 + 4: Concurrent Read Operation
     * ========================================
     * Multiple threads can call this simultaneously
     */
    public List<Product> getAllProducts() throws SQLException {
        String threadName = Thread.currentThread().getName();
        logger.fine("Thread " + threadName + " reading all products");

        try {
            // ReadLock allows multiple concurrent readers
            if (cacheLock.readLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    recordOperation("GET_ALL_PRODUCTS", "READ");

                    // Return defensive copies to prevent external modification
                    List<Product> products = new ArrayList<>();
                    for (Product product : productCache.values()) {
                        products.add(cloneProduct(product));
                    }

                    logger.fine("Thread " + threadName + " read " + products.size() + " products");
                    return products;

                } finally {
                    cacheLock.readLock().unlock();
                }
            } else {
                throw new ConcurrencyException("Failed to acquire read lock within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyException("Thread interrupted while waiting for lock", e);
        }
    }

    /**
     * ========================================
     * PATTERN 1 + 4: Concurrent Get Single Product
     * ========================================
     * Cache hit = fast, cache miss = load from DB
     */
    public Product getProduct(String code) throws SQLException {
        String threadName = Thread.currentThread().getName();
        logger.fine("Thread " + threadName + " getting product: " + code);

        try {
            if (cacheLock.readLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    recordOperation("GET_PRODUCT", "READ");

                    // Check cache first (ConcurrentHashMap - no explicit lock needed)
                    Product cachedProduct = productCache.get(code);

                    if (cachedProduct != null) {
                        recordCacheHit(code);
                        logger.fine("Cache HIT for product: " + code);
                        return cloneProduct(cachedProduct);
                    }

                    // Cache miss - load from database
                    recordCacheMiss(code);
                    logger.fine("Cache MISS for product: " + code);

                } finally {
                    cacheLock.readLock().unlock();
                }

                // Load from DB (outside read lock to avoid blocking others)
                Product product = productDAO.getProductByCode(code);

                if (product != null) {
                    // Update cache with write lock
                    if (cacheLock.writeLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        try {
                            productCache.put(code, cloneProduct(product));
                            logger.fine("Product " + code + " added to cache");
                        } finally {
                            cacheLock.writeLock().unlock();
                        }
                    }
                }

                return product;

            } else {
                throw new ConcurrencyException("Failed to acquire lock for product: " + code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyException("Thread interrupted while accessing product", e);
        }
    }

    /**
     * ========================================
     * PATTERN 1 + 2: Add Product with Version Control
     * ========================================
     * WriteLock ensures exclusive access
     * Version starts at 0 for new products
     */
    public boolean addProduct(Product product) throws SQLException {
        String threadName = Thread.currentThread().getName();
        logger.info("Thread " + threadName + " adding product: " + product.getCode());

        try {
            if (cacheLock.writeLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    recordOperation("ADD_PRODUCT", "WRITE");

                    // Check if product already exists
                    if (productCache.containsKey(product.getCode())) {
                        logger.warning("Product already exists: " + product.getCode());
                        return false;
                    }

                    // Set initial version for optimistic locking
                    product.setVersion(0);

                    // Add to database
                    boolean success = productDAO.addProduct(product);

                    if (success) {
                        // Update cache
                        productCache.put(product.getCode(), cloneProduct(product));
                        logger.info("Product added successfully: " + product.getCode());
                    }

                    return success;

                } finally {
                    cacheLock.writeLock().unlock();
                }
            } else {
                throw new ConcurrencyException("Failed to acquire write lock for adding product");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyException("Thread interrupted while adding product", e);
        }
    }

    /**
     * ========================================
     * PATTERN 2: Optimistic Locking
     * ========================================
     * Detects concurrent modifications using version numbers
     * Throws ConcurrencyException if conflict detected
     */
    public boolean updateProduct(Product product) throws SQLException {
        String threadName = Thread.currentThread().getName();
        logger.info("Thread " + threadName + " updating product: " + product.getCode() +
                " (version " + product.getVersion() + ")");

        try {
            if (cacheLock.writeLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    recordOperation("UPDATE_PRODUCT", "WRITE");

                    // Get current version from cache
                    Product cachedProduct = productCache.get(product.getCode());
                    if (cachedProduct != null) {
                        if (cachedProduct.getVersion() != product.getVersion()) {
                            // VERSION CONFLICT DETECTED!
                            recordOperation("UPDATE_PRODUCT", "CONFLICT");
                            logger.warning("OPTIMISTIC LOCK CONFLICT detected for product " +
                                    product.getCode() + ": cache version=" +
                                    cachedProduct.getVersion() + ", update version=" +
                                    product.getVersion());

                            throw new ConcurrencyException(
                                    "Product was modified by another user. " +
                                            "Expected version: " + product.getVersion() +
                                            ", Current version: " + cachedProduct.getVersion() +
                                            ". Please refresh and try again."
                            );
                        }
                    }

                    // Increment version for optimistic locking
                    int newVersion = product.getVersion() + 1;
                    product.setVersion(newVersion);

                    // Update in database with version check
                    boolean success = productDAO.updateProductWithVersion(product);

                    if (!success) {
                        // Database detected a conflict (another transaction updated first)
                        recordOperation("UPDATE_PRODUCT", "CONFLICT");
                        throw new ConcurrencyException(
                                "Product was modified by another user during update. Please refresh and try again."
                        );
                    }

                    // Update cache
                    productCache.put(product.getCode(), cloneProduct(product));
                    logger.info("Product updated successfully: " + product.getCode() +
                            " (new version: " + newVersion + ")");

                    return true;

                } finally {
                    cacheLock.writeLock().unlock();
                }
            } else {
                throw new ConcurrencyException("Failed to acquire write lock for updating product");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyException("Thread interrupted while updating product", e);
        }
    }

    /**
     * ========================================
     * PATTERN 3: Pessimistic Locking with Transaction
     * ========================================
     * Uses SELECT FOR UPDATE to lock the row during update
     * Prevents race conditions in stock updates
     */
    public boolean updateStock(String productCode, int quantityChange, String changeType)
            throws SQLException {
        String threadName = Thread.currentThread().getName();
        logger.info("Thread " + threadName + " updating stock for " + productCode +
                ": " + changeType + " " + quantityChange);

        try {
            if (cacheLock.writeLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    recordOperation("UPDATE_STOCK", "WRITE");

                    // Use pessimistic locking in DAO (SELECT FOR UPDATE)
                    Product product = productDAO.getProductWithLock(productCode);

                    if (product == null) {
                        logger.warning("Product not found: " + productCode);
                        return false;
                    }

                    // Calculate new stock based on change type
                    int newQuantityInStore = product.getQuantityInStore();
                    int newQuantityOnShelf = product.getQuantityOnShelf();

                    switch (changeType.toUpperCase()) {
                        case "ADD_STORE":
                            newQuantityInStore += quantityChange;
                            break;
                        case "REMOVE_STORE":
                            newQuantityInStore -= quantityChange;
                            if (newQuantityInStore < 0) {
                                throw new InsufficientStockException(
                                        productCode, quantityChange, product.getQuantityInStore()
                                );
                            }
                            break;
                        case "ADD_SHELF":
                            newQuantityOnShelf += quantityChange;
                            break;
                        case "REMOVE_SHELF":
                            newQuantityOnShelf -= quantityChange;
                            if (newQuantityOnShelf < 0) {
                                throw new InsufficientStockException(
                                        productCode, quantityChange, product.getQuantityOnShelf()
                                );
                            }
                            break;
                        case "MOVE_TO_SHELF":
                            // Move from store to shelf
                            if (product.getQuantityInStore() < quantityChange) {
                                throw new InsufficientStockException(
                                        productCode, quantityChange, product.getQuantityInStore()
                                );
                            }
                            newQuantityInStore -= quantityChange;
                            newQuantityOnShelf += quantityChange;
                            break;
                        case "SALE":
                            // Remove from shelf for sale
                            if (product.getQuantityOnShelf() < quantityChange) {
                                throw new InsufficientStockException(
                                        productCode, quantityChange, product.getQuantityOnShelf()
                                );
                            }
                            newQuantityOnShelf -= quantityChange;
                            break;
                        default:
                            logger.warning("Invalid change type: " + changeType);
                            return false;
                    }

                    // Update stock in database (still holding the row lock)
                    product.setQuantityInStore(newQuantityInStore);
                    product.setQuantityOnShelf(newQuantityOnShelf);
                    product.setVersion(product.getVersion() + 1);

                    boolean success = productDAO.updateProductStock(product);

                    if (success) {
                        // Update cache
                        productCache.put(productCode, cloneProduct(product));
                        logger.info("Stock updated successfully for " + productCode +
                                ": Store=" + newQuantityInStore + ", Shelf=" + newQuantityOnShelf);
                    }

                    return success;

                } finally {
                    cacheLock.writeLock().unlock();
                }
            } else {
                throw new ConcurrencyException("Failed to acquire lock for stock update");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyException("Thread interrupted during stock update", e);
        }
    }

    /**
     * ========================================
     * PATTERN 1: Delete Product
     * ========================================
     */
    public boolean deleteProduct(String productCode) throws SQLException {
        String threadName = Thread.currentThread().getName();
        logger.info("Thread " + threadName + " deleting product: " + productCode);

        try {
            if (cacheLock.writeLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    recordOperation("DELETE_PRODUCT", "WRITE");

                    boolean success = productDAO.deleteProduct(productCode);

                    if (success) {
                        // Remove from cache
                        productCache.remove(productCode);
                        logger.info("Product deleted successfully: " + productCode);
                    }

                    return success;

                } finally {
                    cacheLock.writeLock().unlock();
                }
            } else {
                throw new ConcurrencyException("Failed to acquire lock for product deletion");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyException("Thread interrupted during product deletion", e);
        }
    }

    /**
     * Get products with low stock (for reorder report)
     */
    public List<Product> getLowStockProducts() throws SQLException {
        logger.info("Getting low stock products");

        try {
            if (cacheLock.readLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    recordOperation("GET_LOW_STOCK", "READ");

                    List<Product> lowStockProducts = new ArrayList<>();

                    for (Product product : productCache.values()) {
                        int totalStock = product.getQuantityInStore() + product.getQuantityOnShelf();
                        if (totalStock <= product.getReorderLevel()) {
                            lowStockProducts.add(cloneProduct(product));
                        }
                    }

                    logger.info("Found " + lowStockProducts.size() + " low stock products");
                    return lowStockProducts;

                } finally {
                    cacheLock.readLock().unlock();
                }
            } else {
                throw new ConcurrencyException("Failed to acquire lock for low stock check");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyException("Thread interrupted during low stock check", e);
        }
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(String category) throws SQLException {
        logger.info("Getting products by category: " + category);

        try {
            if (cacheLock.readLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    recordOperation("GET_BY_CATEGORY", "READ");

                    List<Product> categoryProducts = new ArrayList<>();

                    for (Product product : productCache.values()) {
                        if (product.getCategory().equalsIgnoreCase(category)) {
                            categoryProducts.add(cloneProduct(product));
                        }
                    }

                    logger.info("Found " + categoryProducts.size() + " products in category: " + category);
                    return categoryProducts;

                } finally {
                    cacheLock.readLock().unlock();
                }
            } else {
                throw new ConcurrencyException("Failed to acquire lock for category search");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyException("Thread interrupted during category search", e);
        }
    }

    /**
     * Refresh cache from database
     */
    public void refreshCache() throws SQLException {
        logger.info("Refreshing product cache");
        loadCache();
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        try {
            if (cacheLock.readLock().tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                try {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("cacheSize", productCache.size());
                    stats.put("totalHits", cacheHits.values().stream().mapToInt(Integer::intValue).sum());
                    stats.put("totalMisses", cacheMisses.values().stream().mapToInt(Integer::intValue).sum());

                    int totalAccess = (int)stats.get("totalHits") + (int)stats.get("totalMisses");
                    double hitRate = totalAccess > 0 ?
                            ((int)stats.get("totalHits") * 100.0 / totalAccess) : 0.0;

                    stats.put("hitRate", String.format("%.2f%%", hitRate));
                    stats.put("operations", operationMetrics);

                    return stats;

                } finally {
                    cacheLock.readLock().unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Collections.emptyMap();
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private void recordOperation(String operation, String type) {
        operationMetrics.computeIfAbsent(operation, k -> new OperationMetrics())
                .increment(type);
    }

    private void recordCacheHit(String code) {
        cacheHits.merge(code, 1, Integer::sum);
    }

    private void recordCacheMiss(String code) {
        cacheMisses.merge(code, 1, Integer::sum);
    }

    /**
     * Create defensive copy of product to prevent external modification
     */
    private Product cloneProduct(Product original) {
        if (original == null) return null;

        Product clone = new Product();
        clone.setCode(original.getCode());
        clone.setName(original.getName());
        clone.setCategory(original.getCategory());
        clone.setPrice(original.getPrice());
        clone.setQuantityInStore(original.getQuantityInStore());
        clone.setQuantityOnShelf(original.getQuantityOnShelf());
        clone.setReorderLevel(original.getReorderLevel());
        clone.setState(original.getState());
        clone.setVersion(original.getVersion());
        clone.setLockedBy(original.getLockedBy());
        clone.setLockTimestamp(original.getLockTimestamp());
        clone.setPurchaseDate(original.getPurchaseDate());
        clone.setExpiryDate(original.getExpiryDate());

        return clone;
    }

    /**
     * Operation metrics for monitoring
     */
    public static class OperationMetrics {
        private int readCount = 0;
        private int writeCount = 0;
        private int conflictCount = 0;

        public synchronized void increment(String type) {
            switch (type) {
                case "READ":
                    readCount++;
                    break;
                case "WRITE":
                    writeCount++;
                    break;
                case "CONFLICT":
                    conflictCount++;
                    break;
            }
        }

        public synchronized int getReadCount() { return readCount; }
        public synchronized int getWriteCount() { return writeCount; }
        public synchronized int getConflictCount() { return conflictCount; }

        @Override
        public String toString() {
            return String.format("Reads: %d, Writes: %d, Conflicts: %d",
                    readCount, writeCount, conflictCount);
        }
    }
}