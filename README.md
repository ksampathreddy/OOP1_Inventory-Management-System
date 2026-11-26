# StockPro - Inventory and Stock Management System for Retailers (OOP1 Assignment 2026/27)

## Folders
- `src/`          the application (packages `stockpro` and `stockpro.app`)
- `extra-java27/` extra marks: JEP 532 primitive patterns (needs JDK 27)
- `tests/`        plain-Java edge-case checks
- `Report.docx` / `Report.pdf`   the report (same content, two formats)
- `slides/`       the slides for the video
- `screencast/`   put the video here

## Build and run (JDK 21 or newer)
    mkdir out
    javac -d out src/stockpro/*.java src/stockpro/app/*.java
    java -cp out stockpro.app.Main            # starts the menu

## Menu
    1 Add product       7 Expiring items
    2 View all products 8 Transaction history
    3 Search            9 Stock valuation
    4 Restock          10 Change a price
    5 Sell             11 Product details
    6 Low-stock report 12 Java concepts (call-by-value, defensive copying, lambdas)
    0 Exit

Product ids start at 1000 (Milk 2L is 1000, USB-C Charger is 1004).

## Run the edge-case checks
    javac -cp out -d out tests/stockpro/tests/EdgeCaseChecks.java
    java  -cp out stockpro.tests.EdgeCaseChecks

## Extra marks (JDK 27 only)
    cd extra-java27
    javac --release 27 --enable-preview PrimitivePatternsDemo.java
    java  --enable-preview PrimitivePatternsDemo
