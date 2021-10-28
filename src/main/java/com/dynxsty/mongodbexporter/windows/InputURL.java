package com.dynxsty.mongodbexporter.windows;

import com.dynxsty.mongodbexporter.Constants;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import static javax.swing.JOptionPane.showMessageDialog;

public class InputURL extends JFrame {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(InputURL.class);

    private Image img;
    private JTextField textField;

    public InputURL() {

        setTitle("MongoDB Exporter " + Constants.VERSION);

        try { img = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/mongodb.png"));
        } catch (Exception e) { e.printStackTrace(); }

        setIconImage(img);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setResizable(false);

        drawComponents();
        setVisible(true);
    }

    private void drawComponents() {

        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel();
        titleLabel.setText("MongoDB Exporter");
        titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getSize() + 20f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        textField = new JTextField();
        textField.setText("Paste your MongoDB Connection String");
        textField.setToolTipText("Paste your MongoDB Connection String");
        textField.setHorizontalAlignment(SwingConstants.LEFT);
        textField.addActionListener(confirmAction);
        textField.addMouseListener(new MouseAdapter() {

            boolean firstClick = true;

            @Override
            public void mouseClicked(MouseEvent e) {
                if (firstClick) textField.setText("");
                firstClick = false;
            }
        });

        contentPanel.add(textField, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel();
        buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttonBar.setLayout(new GridBagLayout());
        ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
        ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

        JLabel gitHubLabel = new JLabel();
        gitHubLabel.setText("GitHub");
        gitHubLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                try { Desktop.getDesktop().browse(new URI(Constants.GITHUB_LINK));
                } catch (Exception exception) { exception.printStackTrace(); }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                gitHubLabel.setText("<html><a href=''>GitHub</a></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                gitHubLabel.setText("GitHub");
            }
        });

        buttonBar.add(gitHubLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

        JButton confirmButton = new JButton();
        confirmButton.setText("Connect");
        confirmButton.addActionListener(confirmAction);

        buttonBar.add(confirmButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

        JButton exitButton = new JButton();
        exitButton.setText("Exit");
        exitButton.addActionListener(e -> { dispose(); System.exit(0); });

        buttonBar.add(exitButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(buttonBar, BorderLayout.SOUTH);

        contentPane.add(panel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
    }

    Action confirmAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                MongoClientURI uri = new MongoClientURI(textField.getText());
                MongoClient mongoClient = new MongoClient(uri);

                logger.info("Successfully connected to Database!");
                new SelectCollections(mongoClient);

            } catch (Exception exception) {
                JOptionPane.showMessageDialog(null, exception.getMessage(), "An Error occurred", JOptionPane.ERROR_MESSAGE);
            }
        }
    };
}
