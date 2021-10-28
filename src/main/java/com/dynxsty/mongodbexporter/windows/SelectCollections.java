package com.dynxsty.mongodbexporter.windows;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class SelectCollections extends JFrame {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SelectCollections.class);

    private final MongoClient client;
    private Map<String, String> checkedCollections = new HashMap<>();
    private Image img;

    public SelectCollections(MongoClient mongoClient) {

        this.client = mongoClient;

        setTitle("Choose collections");

        try { img = ImageIO.read(getClass().getClassLoader().getResourceAsStream("images/mongodb.png")); }
        catch (Exception e) { e.printStackTrace(); }

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

        JPanel dialogPane = new JPanel();
            dialogPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            dialogPane.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel();
            titleLabel.setText("Select Collections");
            titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getSize() + 20f));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dialogPane.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel hSpacer = new JPanel(null);
        contentPanel.add(hSpacer);

        JTabbedPane tabPane = new JTabbedPane();
        logger.info("Fetching collections...");

        for (var db : client.listDatabaseNames()) {
            if (db.equals("admin") || db.equals("local")) continue;

            JPanel tabPanel = new JPanel();
            tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));

            tabPane.addTab(db, tabPanel);

            for (var col : client.getDatabase(db).listCollectionNames()) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setText(col);

                checkBox.addItemListener(e -> {

                    if (e.getStateChange() == ItemEvent.SELECTED) checkedCollections.put(col, db);
                    else if (e.getStateChange() == ItemEvent.DESELECTED)
                        checkedCollections.remove(col, db);

                });

                tabPanel.add(checkBox);
            }
        }

        contentPanel.add(tabPane);
        dialogPane.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonBar = new JPanel();
            buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
            buttonBar.setLayout(new GridBagLayout());
            ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 85, 80};
            ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1, 0, 0};

        JButton confirmButton = new JButton();
            confirmButton.setText("Confirm");
            confirmButton.addActionListener(confirmAction);

            buttonBar.add(confirmButton, new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

        JButton cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(e -> dispose());

            buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));


            dialogPane.add(buttonBar, BorderLayout.SOUTH);
            contentPane.add(dialogPane, BorderLayout.CENTER);

            pack();
            setLocationRelativeTo(getOwner());
    }

    private File saveToDirectory() {
        JFileChooser f = new JFileChooser();
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        f.showSaveDialog(null);

        return f.getSelectedFile();
    }

    Action confirmAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (checkedCollections.isEmpty()) {
                JOptionPane.showMessageDialog(new JFrame(), "You need to check at least one collection!");
                return;
            }
            String path = saveToDirectory().getPath();

            JsonWriterSettings settings = JsonWriterSettings.builder()
                .outputMode(JsonMode.RELAXED)
                .indent(true)
                .build();
            dispose();

            for (var colDoc : checkedCollections.entrySet()) {

                StringBuilder sb = new StringBuilder();
                try {

                MongoCursor<Document> it = client.getDatabase(colDoc.getValue()).getCollection(colDoc.getKey()).find().iterator();
                    FileWriter file = new FileWriter(path + "/" + colDoc.getKey() + ".json");
                    sb.append("[");

                while (it.hasNext()) {
                    sb.append(it.next().toJson(settings));
                    sb.append(",");
                }
                    file.write(sb.substring(0, sb.length() - 1) + "]");
                    file.flush();
                    file.close();

                    logger.info("Successfully exported Collection {} ({})", colDoc.getKey(), path + "\\" + colDoc.getKey() + ".json");
                } catch (Exception exception) { exception.printStackTrace(); }
            }
        }
    };
}