# Velmora Launchpad - component integration

The launchpad has seven tiles, one per group-project component. All seven are
now live; clicking one opens that member's component.

| Tile | Component | Package | Opens |
|---|---|---|---|
| Finance & Reporting | original app | `com.ecommerce.finance` | same window |
| Delivery & Logistics | Talha | `uk.ac.brunel.delivery` | same window, has a back button |
| Orders Tracking | VelmoraJavaFX-Maven | `velmora` | its own window |
| Customers | GroupProject (gwanhee) | `org.gwanhee.groupproject` | its own window |
| Warehouse | Assessment_B | `com.nc1605.inventorywarehouse` | its own window |
| Reviews & Ratings | Ratings-Reviews | `Ratings_Reviews.Ratings_Reviews` | its own window |
| Products | Products (Eclipse) | `Ecommerce.Products` | its own window |

## The five guest components were not modified

Not one line of `velmora`, `org.gwanhee.groupproject`,
`com.nc1605.inventorywarehouse` or `Ratings_Reviews.Ratings_Reviews` was
edited. Their source files are byte-for-byte what was supplied.

`Ecommerce.Products` has exactly one change: the database path constant in
`Controller.java`, because the original pointed at a drive that does not exist
on this machine. See the Products database section below. Every other file of
that component is byte-for-byte as supplied.

That is possible because each of them already has its own `Application` class
with a `start(Stage)` method. The launchpad simply creates a fresh window and
calls that method - the exact same call JavaFX makes when the component runs on
its own. Each component then sets its own title, scenes, stylesheet and window
size, and shows itself, with no knowledge that a launchpad exists.

Consequences, all deliberate:

- **Each runs in a separate window.** The launchpad stays open behind them, so
  several components can be open at once and none of them can interfere with
  another's scenes or window size.
- **Closing a component window leaves everything else running.** Clicking the
  same tile again brings that window back to the front rather than starting the
  component over.
- **No back button inside them**, because adding one would have meant editing
  their FXML and controllers. The launchpad is already visible behind the
  window - closing the window is the way back.
- **If one component throws on startup, it is caught** and only that window
  closes. The launchpad and every other component keep working.

## What was edited (only launchpad-side files)

**`MainApp.java`** - builds a `tile id -> action` map, and a new
`openComponentWindow(...)` that creates one `Stage` per component, hands it to
that component's own `Application`, and reuses the window on later clicks.

**`launchpad/LaunchpadView.java`** - now takes that map instead of fixed
`Runnable` fields. A tile is live when the map has an entry for its id, so
wiring a future component is a one-line change in `MainApp`. The old
constructors are kept so nothing else breaks.

Nothing else changed. The Finance & Reporting and Delivery & Logistics
behaviour from the previous build is exactly as it was.

## Files deliberately not copied

Three `module-info.java` files - from the Warehouse, Reviews & Ratings and
Products components. Each declares its component as its own Java module, which
only makes sense when that component is built alone. In a combined classpath
build their presence stops the whole project from compiling. No component
source file was changed - only these build descriptors were left out.

The five `pom.xml` files were likewise not copied, since the combined project
builds from one pom. No new dependencies were needed: JavaFX and sqlite-jdbc
were already there. Mac metadata (`__MACOSX`, `.DS_Store`) from the
Ratings-Reviews archive was stripped.

## Products database - fixed

`Ecommerce.Products.Controller` originally connected to:

```
jdbc:sqlite:E:/velmora_products.db/SQLite database files.db
```

That is an absolute path on the original author's machine, so on any other
computer every login and product action failed with "Database error: path to
'E:/velmora_products.db/SQLite ...'".

The supplied database file now ships in this project's root folder as
`SQLite database files.db`, and the `DB` constant resolves to that copy
instead: it looks in the working directory, then the folder above it. That
one constant plus a small private helper is **the only difference** between
this copy of the Products component and the version supplied. Its queries,
screens, controllers and DAO are untouched.

The database holds 19 products, 5 categories, 19 images and two accounts.
Both were verified against the component's own login query:

| Portal | Email | Password |
|---|---|---|
| Admin | `admin@velmora.com` | `admin123` |
| Customer | `customer@example.com` | `customer123` |

The admin portal enforces an `@velmora.com` address, which the admin account
satisfies.

## Known issue: the Customers component cannot reach its database

`org.gwanhee.groupproject.DataBaseConnection` connects to:

```
jdbc:sqlite:C:/Users/zxcvb/OneDrive/database_Example.db
```

That is a path on the original author's own laptop, the `.db` file was not
included, and that component's code never creates its tables - it only reads
and writes `Accounts` and `PaymentMethod`. So the Customers tile opens and the
screens render, but log in, register and card actions will fail.

This was left exactly as supplied, as requested. Fixing it means either getting
the real `.db` file from its author, or changing that one line to a local path
and creating the two tables.

## Java levels

Everything builds at Java 17, unchanged from before. The supplied components
targeted Java 21, 21, 11 and 17 in their own poms, but none of them uses any
syntax above 17 - that was checked file by file.

## Running it

Windows: double-click `run.bat`, or `mvnw.cmd clean javafx:run`.
Main class is still `com.ecommerce.finance.MainApp`.

Database files in the project folder: `ecommerce.db` (finance, supplied),
plus `velmora-delivery.db`, `velmora.db` and `inventory_warehouse.db`, each
created and seeded automatically on that component's first run.

Reviews & Ratings creates and seeds `ratings-reviews.db` itself on first run.

Delivery demo logins: `admin` / `Admin123!`, `priya` / `Driver123!`.
Orders demo logins: `demo@velmora.com` / `demo1234`, admin `admin` / `admin123`.
Products logins: `admin@velmora.com` / `admin123` (admin portal),
`customer@example.com` / `customer123` (customer portal).
