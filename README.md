SYOS POS System - Clean Architecture Implementation


A comprehensive Point of Sale (POS) system for Synex Outlet Store (SYOS), demonstrating clean code principles, SOLID design principles, and advanced software architecture patterns. Built as a final year university project showcasing enterprise-level software development practices.

📋 Table of Contents

Overview
Features
Architecture
Design Patterns
Technology Stack
Installation
Usage
Project Structure
Testing
Contributing
License

🎯 Overview
SYOS POS System is a command-line based point of sale application designed to streamline retail operations. It replaces manual billing processes with an automated system that integrates inventory management, sales processing, and comprehensive reporting capabilities.

Key Objectives

Eliminate Manual Errors: Automated calculations and inventory tracking
Improve Efficiency: Reduce customer wait times during peak hours
Real-time Inventory: Automatic stock updates with every transaction
Comprehensive Reporting: Daily sales, stock levels, and reorder alerts
Clean Architecture: Maintainable, testable, and extensible codebase

✨ Features
Sales Management

✅ Fast checkout process with barcode support
✅ Automatic price calculations with tax handling
✅ Cash payment processing with change calculation
✅ Bill generation with detailed itemization
✅ Transaction history and retrieval

Inventory Control

✅ Real-time stock tracking
✅ Automated reorder level alerts (< 50 items)
✅ Expiry date monitoring with alerts
✅ Stock movement tracking (Store → Shelf → Sold)
✅ Batch-wise inventory management

Reporting Suite

✅ Daily sales reports with revenue analysis
✅ Current stock reports by category/state
✅ Low stock alerts and reorder suggestions
✅ Expiring items report with urgency levels
✅ Export reports to file system

User Management

✅ Role-based access control (Admin, Manager, Cashier)
✅ Secure authentication with SHA-256 hashing
✅ User activity logging
✅ Session management

🏗️ Architecture
The system implements Clean Architecture (Onion Architecture) with clear separation of concerns:
┌─────────────────────────────────────────────────┐
│                 Presentation Layer               │
│              (CLI, Presenters, Menu)             │
├─────────────────────────────────────────────────┤
│                Infrastructure Layer              │
│        (Database, Factories, Gateways)           │
├─────────────────────────────────────────────────┤
│                Application Layer                 │
│         (Commands, Services, Reports)            │
├─────────────────────────────────────────────────┤
│                  Domain Layer                    │
│      (Entities, Value Objects, Interfaces)       │
└─────────────────────────────────────────────────┘
SOLID Principles

Single Responsibility: Each class has one reason to change
Open/Closed: Extensible through abstractions, not modifications
Liskov Substitution: Implementations are interchangeable
Interface Segregation: Focused, cohesive interfaces
Dependency Inversion: Depend on abstractions, not concretions

🎨 Design Patterns
The project implements 11 GoF design patterns:

Command Pattern - Encapsulates all user actions as commands
Factory Method - Centralizes object creation (CommandFactory, ServiceFactory)
Singleton - Database connection pool, input reader
Object Pool - Efficient database connection management
Table Data Gateway - Clean database access layer
State Pattern - Item lifecycle (InStore → OnShelf → Sold/Expired)
Builder Pattern - Complex object construction (Item, Bill)
Template Method - Standardized report generation
Composite Pattern - Hierarchical menu system
Visitor Pattern - Bill processing without modifying entities
Decorator Pattern - Enhanced bill functionality for online transactions

🛠️ Technology Stack

Language: Java 17+
Database: MySQL 8.0+
Build Tool: Maven 3.8+
Testing: JUnit 5, Mockito
Logging: SLF4J with Logback
Architecture: Clean Architecture, Domain-Driven Design

📦 Installation
Prerequisites

Java JDK 17 or higher
MySQL Server 8.0 or higher
Maven 3.8 or higher
Git

Setup Instructions

Clone the repository
bashgit clone https://github.com/yourusername/syos-pos-system.git
cd syos-pos-system

Set up the database
bashmysql -u root -p < sql/complete_database_setup.sql

Configure database connection
Edit src/main/resources/config/application.properties:
propertiesdb.username=root
db.password=your_mysql_password

Build the project
bashmvn clean install

Run the application
bashjava -jar target/syos-pos-system-1.0.0-jar-with-dependencies.jar


🚀 Usage
Default Login Credentials
Username Password   Role
admin    admin123   Administrator
manager1 manager123 Manager
cashier1 cashier123 Cashier

Quick Start Guide

Login: Use credentials above
Process Sale: Sales → Create New Sale
Add Stock: Inventory → Add Stock
Generate Reports: Reports → Select report type
Manage Users: User Management → Register User (Admin only)

Sample Workflow
1. Login as cashier1
2. Create new sale
3. Add items: MILK001 (qty: 2), BREAD001 (qty: 1)
4. Enter cash payment: $10.00
5. System calculates change and prints bill
6. Stock automatically updated


📁 Project Structure
syos-pos-system/
├── src/
│   ├── main/
│   │   ├── java/com/syos/
│   │   │   ├── domain/          # Business logic & entities
│   │   │   ├── application/     # Use cases & commands
│   │   │   ├── infrastructure/  # External concerns
│   │   │   └── Main.java       # Entry point
│   │   └── resources/
│   │       └── config/         # Configuration files
│   └── test/                   # Unit & integration tests
├── sql/                        # Database scripts
├── docs/                       # Documentation
├── reports/                    # Generated reports
└── pom.xml                     # Maven configuration


🧪 Testing
The project includes comprehensive test coverage:

Unit Tests: Domain logic, services, commands
Integration Tests: Database operations
Test Patterns: Test Data Builders, Mock Objects


