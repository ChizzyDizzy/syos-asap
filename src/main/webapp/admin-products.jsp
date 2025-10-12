<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.syos.web.model.User" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    String role = (String) session.getAttribute("role");
    if (!"ADMIN".equals(role)) {
        response.sendRedirect(request.getContextPath() + "/cashier-dashboard.jsp");
        return;
    }
    User currentUser = (User) session.getAttribute("user");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Manage Products</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { padding: 20px; }
        .badge-ON_SHELF { background-color: #28a745; }
        .badge-IN_STORE { background-color: #17a2b8; }
        .badge-EXPIRED { background-color: #dc3545; }
        .badge-SOLD_OUT { background-color: #ffc107; color: #000; }
    </style>
</head>
<body>
<div class="container-fluid">
    <h1>Manage Products</h1>
    <p>Welcome, <%= currentUser.getFullName() %></p>
    <hr>

    <button class="btn btn-primary mb-3" onclick="showAddModal()">Add Product</button>

    <table class="table table-striped" id="productsTable">
        <thead class="thead-dark">
        <tr>
            <th>Code</th>
            <th>Name</th>
            <th>Category</th>
            <th>Price</th>
            <th>In Store</th>
            <th>On Shelf</th>
            <th>Total</th>
            <th>State</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody id="tableBody">
        <tr><td colspan="9" class="text-center">Loading...</td></tr>
        </tbody>
    </table>
</div>

<!-- Add/Edit Modal -->
<div class="modal" id="productModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 id="modalTitle">Add Product</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <form id="productForm">
                <div class="modal-body">
                    <input type="hidden" id="isUpdate">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Code *</label>
                                <input type="text" class="form-control" id="itemCode" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Name *</label>
                                <input type="text" class="form-control" id="name" required>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Category</label>
                                <input type="text" class="form-control" id="category" value="General">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Price *</label>
                                <input type="number" step="0.01" class="form-control" id="price" required>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Qty In Store</label>
                                <input type="number" class="form-control" id="quantityInStore" value="0">
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Qty On Shelf</label>
                                <input type="number" class="form-control" id="quantityOnShelf" value="0">
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Reorder Level</label>
                                <input type="number" class="form-control" id="reorderLevel" value="50">
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>State</label>
                                <select class="form-control" id="state">
                                    <option value="IN_STORE">In Store</option>
                                    <option value="ON_SHELF" selected>On Shelf</option>
                                    <option value="EXPIRED">Expired</option>
                                    <option value="SOLD_OUT">Sold Out</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Expiry Date</label>
                                <input type="date" class="form-control" id="expiryDate">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                    <button type="submit" class="btn btn-primary">Save</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const API = '<%= request.getContextPath() %>/admin-products';

    // Load products on page load
    $(document).ready(function() {
        loadProducts();
        $('#productForm').on('submit', saveProduct);
    });

    // Load all products
    function loadProducts() {
        $.ajax({
            url: API,
            method: 'GET',
            success: function(products) {
                console.log('Loaded products:', products);
                displayProducts(products);
            },
            error: function(xhr) {
                console.error('Error:', xhr);
                $('#tableBody').html('<tr><td colspan="9" class="text-center text-danger">Error loading products</td></tr>');
            }
        });
    }

    // Display products in table
    function displayProducts(products) {
        if (!products || products.length === 0) {
            $('#tableBody').html('<tr><td colspan="9" class="text-center">No products found</td></tr>');
            return;
        }

        let html = '';
        products.forEach(function(p) {
            html += '<tr>';
            html += '<td>' + p.itemCode + '</td>';
            html += '<td>' + p.name + '</td>';
            html += '<td>' + p.category + '</td>';
            html += '<td>$' + p.price.toFixed(2) + '</td>';
            html += '<td>' + p.quantityInStore + '</td>';
            html += '<td>' + p.quantityOnShelf + '</td>';
            html += '<td>' + (p.quantityInStore + p.quantityOnShelf) + '</td>';
            html += '<td><span class="badge badge-' + p.state + '">' + p.state + '</span></td>';
            html += '<td>';
            html += '<button class="btn btn-sm btn-info" onclick="editProduct(\'' + p.itemCode + '\')">Edit</button> ';
            html += '<button class="btn btn-sm btn-danger" onclick="deleteProduct(\'' + p.itemCode + '\')">Delete</button>';
            html += '</td>';
            html += '</tr>';
        });
        $('#tableBody').html(html);
    }

    // Show add modal
    function showAddModal() {
        $('#modalTitle').text('Add Product');
        $('#isUpdate').val('false');
        $('#productForm')[0].reset();
        $('#itemCode').prop('readonly', false);
        $('#productModal').modal('show');
    }

    // Edit product
    function editProduct(code) {
        $.ajax({
            url: API + '?code=' + code,
            method: 'GET',
            success: function(p) {
                $('#modalTitle').text('Edit Product');
                $('#isUpdate').val('true');
                $('#itemCode').val(p.itemCode).prop('readonly', true);
                $('#name').val(p.name);
                $('#category').val(p.category);
                $('#price').val(p.price);
                $('#quantityInStore').val(p.quantityInStore);
                $('#quantityOnShelf').val(p.quantityOnShelf);
                $('#reorderLevel').val(p.reorderLevel);
                $('#state').val(p.state);
                $('#expiryDate').val(p.expiryDate || '');
                $('#productModal').modal('show');
            },
            error: function() {
                alert('Error loading product');
            }
        });
    }

    // Delete product
    function deleteProduct(code) {
        if (!confirm('Delete product ' + code + '?')) return;

        $.ajax({
            url: API + '?code=' + code,
            method: 'DELETE',
            success: function() {
                alert('Product deleted');
                loadProducts();
            },
            error: function() {
                alert('Error deleting product');
            }
        });
    }

    // Save product
    function saveProduct(e) {
        e.preventDefault();

        const isUpdate = $('#isUpdate').val() === 'true';
        const data = {
            itemCode: $('#itemCode').val(),
            name: $('#name').val(),
            category: $('#category').val(),
            price: parseFloat($('#price').val()),
            quantityInStore: parseInt($('#quantityInStore').val()) || 0,
            quantityOnShelf: parseInt($('#quantityOnShelf').val()) || 0,
            reorderLevel: parseInt($('#reorderLevel').val()) || 50,
            state: $('#state').val(),
            expiryDate: $('#expiryDate').val() || null
        };

        $.ajax({
            url: API,
            method: isUpdate ? 'PUT' : 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function() {
                $('#productModal').modal('hide');
                alert('Product saved');
                loadProducts();
            },
            error: function(xhr) {
                alert('Error: ' + (xhr.responseJSON ? xhr.responseJSON.error : 'Unknown error'));
            }
        });
    }
</script>
</body>
</html>