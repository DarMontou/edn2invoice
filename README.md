# edn2invoice
Create invoices from an EDN map of customers, projects, and a log of work chunks

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server
    
Then you can generate invoices for the clients via:

http://localhost:3000/client/Bricks
or 
http://localhost:3000/client/Daphnes

## License

Just take it. It's yours.


