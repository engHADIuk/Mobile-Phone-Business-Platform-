# Mobile Phone Business Platform

A full-stack web application for a mobile phone business, built as a group project at Brunel University London (BSc Software Engineering, Group Project B).

## My contribution: Finance & Reporting module
I owned the Finance & Reporting section of the system:
- Built all of the financial functionality and designed the UI/UX for financial data and reports
- Worked with a shared SQL database used by the whole team as a single source of truth, integrated with real-time data so every department saw the same figures
- Linked financial figures across departments so every number in the system is correct and traceable
- Tested the finance section with JUnit and manual checks

## Tech stack
- Java
- Maven (with Maven Wrapper)
- SQLite
- JUnit

## What the system covers
The platform covers products and catalogue, customer accounts, carts and orders, returns, ratings and reviews, and admin reporting. My module ties these together into finance and reporting views.

## How to run
1. Clone the repository: `git clone https://github.com/engHADIuk/Mobile-Phone-Business-Platform.git`
2. On Windows, start the application with `run.bat`. Or import the folder into Eclipse via File → Import → Existing Maven Projects.
3. Run the tests with `./mvnw test` (on Windows: `mvnw.cmd test`).

The SQLite databases are included with sample data, so it works straight away.

## Demo logins
The databases contain demo data only.
- Customer: `demo@velmora.com` / `demo1234`
- Admin: `admin` / `admin123`

## Team
Group project completed with my Brunel University London teammates. I was responsible for the Finance & Reporting module.
