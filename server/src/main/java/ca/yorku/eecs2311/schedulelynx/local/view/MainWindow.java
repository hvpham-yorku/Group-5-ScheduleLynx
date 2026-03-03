package ca.yorku.eecs2311.schedulelynx.local.view;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final JPanel mainContentPanel; // contains the login view and subCardLayout
    private final JPanel viewContentPanel; // contains all the other views (home, planner, etc.)

    private final CardLayout mainCardLayout;
    private final CardLayout viewCardLayout;

    public MainWindow() {

        setTitle("Schedule Lynx");

        int minWidth = 600;
        int minHeight = 400;
        var minSize = new Dimension(minWidth, minHeight);
        setMinimumSize(minSize);

        setLocationByPlatform(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mainCardLayout = new CardLayout();
        viewCardLayout = new CardLayout();

        mainContentPanel = new JPanel(mainCardLayout);
        viewContentPanel = new JPanel(viewCardLayout);

        initContent();
    }

    /** Instantiates and initializes content from most to least nested component. */
    private void initContent() {

        initViewPanel();
        var buttonPanel = initButtonPanel();

        var contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(viewContentPanel, BorderLayout.CENTER);

        // TODO: Add login to contentPanel... mainContentPanel.add(loginPanel, "loginPanel");
        mainContentPanel.add(contentPanel, "contentPanel");

        this.add(mainContentPanel);
    }

    private void initViewPanel() {

        initButtonPanel();

        viewContentPanel.add(new Home()     , "home");
        viewContentPanel.add(new Dashboard(), "dash");
        viewContentPanel.add(new Planner()  , "plan");
        viewContentPanel.add(new Features() , "feat");
    }

    private Container initButtonPanel() {

        var buttonPanel = Box.createHorizontalBox();

        var homeButton = new Button("Home");
        var dashButton = new Button("Dashboard");
        var planButton = new Button("Planner");
        var featButton = new Button("Features");
        var loutButton = new Button("Log Out");

        homeButton.addActionListener(ignored -> viewCardLayout.show(viewContentPanel, "home"));
        dashButton.addActionListener(ignored -> viewCardLayout.show(viewContentPanel, "dash"));
        planButton.addActionListener(ignored -> viewCardLayout.show(viewContentPanel, "plan"));
        featButton.addActionListener(ignored -> viewCardLayout.show(viewContentPanel, "feat"));
        loutButton.addActionListener(ignored -> Toolkit.getDefaultToolkit().beep());

        buttonPanel.add(homeButton);
        buttonPanel.add(dashButton);
        buttonPanel.add(planButton);
        buttonPanel.add(featButton);
        buttonPanel.add(loutButton);

        return buttonPanel;
    }

}
