# The esoco-gwt project

This project contains the esoco GWT framework library. It is the bridge between the esoco-business framework and GEWT, combining the business back-end (processes and persistent entities) with a GWT user interface into a consistent and easy to use framework for building client-server web applications. It handles the communication between the server side and the client running in a web browser, automatically transforming business data into visual representations in the browser and transferring user input to the server for process execution and persistence. It also features high-level components that are the building blocks for the implementation of complete web applications.

The GWT framework also contains the base classes and core elements necessary to create a configurable application that can be deployed into an application server which uses a connection to a standard relational database like PostgreSQL or MySQL. The GwtApplication already supports the automatic historization of changes in persistent data, database and file logging, and background processing. It is only necessary to implement the business-specific entities and processes to create a full application.

# License

This project is licensed under the Apache 2.0 license (see LICENSE file for details).  