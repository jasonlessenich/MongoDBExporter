package com.dynxsty.mongodbexporter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.dynxsty.mongodbexporter.windows.InputURL;
import com.formdev.flatlaf.FlatDarculaLaf;
import org.slf4j.LoggerFactory;

import javax.swing.*;

class Main extends JFrame {

    public static void main(String[] args) {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.ERROR);

        try { UIManager.setLookAndFeel( new FlatDarculaLaf() );
        } catch (Exception e) { e.printStackTrace(); }

         new InputURL();
        }
    }
