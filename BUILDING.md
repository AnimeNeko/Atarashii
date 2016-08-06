Atarashii! - Building & developing the application
=====================================
These steps below will help you to build the application.
Most of the steps should be easy but do not forget to add the lombok plugin!
It is possible to build the app without that plugin but it will be hard to develop without it.

Fetching the Code
-----------------
We recommend to use a git terminal or if you are not very experienced Source tree.
Source tree can be downloaded at: https://www.sourcetreeapp.com and the FAQ is available at: https://www.sourcetreeapp.com/faq


Importing the Project to Android Studio
---------------------------------------
We highly recommend to use Android Studio for Atarashii! development.
Make sure you have Android Studio installed. If you do not, follow the
instructions at https://developer.android.com/studio/install.html
The download package is available at https://developer.android.com/studio/index.html

Import the project with "File > Import Project..." or selecting "Import
Project..." on the welcome screen. Select the root directory of the source
you have cloned. Gradle should load all required dependencies except <b>Project Lombok</b>!

Installing the Project Lombok plugin
---------------------------------------
Atarashii uses Project Lombok for the Get and Set methods which is used a lot.
To install the plugin: File -> Settings -> Plugins -> browse Repositories.
Enter Lombok in the searchfield and there will be one result called "Lombok Plugin".
Press on install and restart android studio.


You should now be able to successfully build and develop Atarashii!