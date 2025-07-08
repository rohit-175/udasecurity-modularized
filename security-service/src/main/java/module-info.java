module security.service {
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.security.core;
    requires image.service;
    requires com.google.common;
    requires com.google.gson;
    //requires com.miglayout.swing;
    requires com.miglayout.swing;
    requires java.desktop;
    requires java.prefs;
//    requires static miglayout.swing;

    exports com.udacity.udasecurity;
    exports com.udacity.udasecurity.service;
    exports com.udacity.udasecurity.application;
    exports com.udacity.udasecurity.data;

    opens com.udacity.udasecurity.service to org.junit.platform.commons;
    opens com.udacity.udasecurity.application to org.junit.platform.commons;
    opens com.udacity.udasecurity.data to org.junit.platform.commons;
}
