/**
 * Program Name: PandemicModelerApp.java
 * Purpose: This is the main program for the project. It will simulate an epidemic infection visually...
 * Coders: Ethan Rivers 1015561, Jefferson Gilbert 1140941 & Daniel Allison 1130809
 * Date: Aug 1, 2024
 */
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class PandemicModelerApp extends JPanel {

    private final int WIDTH = 600, HEIGHT = 500; // Size of the simulation panel
    private final int LAG_TIME = 20; // Time in milliseconds between re-paints of screen
    private Timer timer; // Timer object that will fire events every LAG_TIME interval
    private final int DOT_SIZE = 10; // Size of the person dot to be drawn

    private int personCount; // Total number of Person objects
    private Person[] persons;

    private final int MAX_REPAINTS_ALLOWED = 450;
    private int repaintCounter = 0; 

    private JButton pauseBtn;
    private JButton resumeBtn;
    private JButton restartBtn; 

    private JPanel simulationPanel;
    private JPanel toolPanel;
    private JPanel buttonPanel;

    // Dashboard labels (used in toolPanel)
    private JLabel totalPeopleLabel; 
    private JLabel totalInfectedLabel;
    private JLabel unvaccinatedInfectedLabel;
    private JLabel oneShotInfectedLabel;
    private JLabel twoShotInfectedLabel;
    private JLabel threeShotInfectedLabel;
    private JLabel naturalImmunityInfectedLabel;
    private JLabel recoveredLabel;
    private JLabel deadLabel;
    private JLabel dayCounterLabel;  

    // Holds amount of people for simulation based on user % input
    private int unvaccinatedCount = 0;
    private int oneShotCount = 0;
    private int twoShotCount = 0;
    private int threeShotCount = 0;
    private int naturalImmunityCount = 0;

    // Tool panel counters for real-time updates (used for calculations of toolPanel stats)
    private int totalInfected = 0;
    private int unvaccinatedInfected = 0;
    private int oneShotInfected = 0;
    private int twoShotInfected = 0;
    private int threeShotInfected = 0;
    private int naturalImmunityInfected = 0;
    private int recovered = 0;

    private int dead = 0;
    private int unvaccinatedDeaths = 0;
    private int oneShotDeaths = 0;
    private int twoShotDeaths = 0;
    private int threeShotDeaths = 0;
    private int naturalImmunityDeaths = 0;

    // Grand total counters for people who contracted the disease (For final report)
    private int gtInfected;
    private int gtUnvaccinatedContractions;
    private int gtOneShotContractions;
    private int gtTwoShotContractions;
    private int gtThreeShotContractions;
    private int gtNaturallyImmuneRecontractions;

    // Store the last selected values for sliders and radio buttons
    private int lastPopulationSize = 100;
    private int lastUnvaccinatedPercent = 100;
    private int lastOneShotPercent = 0;
    private int lastTwoShotPercent = 0;
    private int lastThreeShotPercent = 0;
    private int lastNaturalImmunityPercent = 0;

    // Constructor
    public PandemicModelerApp() {
      // Create Timer and register a listener for it
      this.timer = new Timer(LAG_TIME, new MoveListener());

      // Initialize buttons
      pauseBtn = new JButton("Pause");
      resumeBtn = new JButton("Resume");
      restartBtn = new JButton("Restart"); // Initialize restart button
      resumeBtn.setEnabled(false);
      pauseBtn.setEnabled(false);
      JButton aboutBtn = new JButton("About");

      // Add action listeners to buttons
      pauseBtn.addActionListener(e -> pauseSimulation());
      resumeBtn.addActionListener(e -> resumeSimulation());
      restartBtn.addActionListener(e -> restartSimulation()); // Action for restart button
      aboutBtn.addActionListener(e -> showAboutDialog());

      // Initialize panels
      simulationPanel = new JPanel() {
          @Override
          protected void paintComponent(Graphics g) {
              super.paintComponent(g);
              if (persons != null) {
                  for (Person person : persons) {
                      g.setColor(person.getColor());
                      g.fillOval(person.getxCoordinate(), person.getyCoordinate(), person.getDotDiameter(), person.getDotDiameter());
                  }
              }
              g.setColor(Color.BLACK);
              g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
          }
      };
      simulationPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
      simulationPanel.setBackground(Color.GRAY);
      simulationPanel.setBorder(new LineBorder(Color.BLACK, 5));

      toolPanel = new JPanel();
      toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));

      buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5)); // Grid layout with 2 rows, 2 columns, and gaps of 5 pixels
      buttonPanel.add(pauseBtn);
      buttonPanel.add(resumeBtn);
      buttonPanel.add(aboutBtn);
      buttonPanel.add(restartBtn); // Add restart button to the panel

      // Add components to the toolPanel
      toolPanel.add(Box.createVerticalStrut(10)); // Add some vertical space at the top
      toolPanel.add(createDashboardPanel());
      toolPanel.add(Box.createVerticalStrut(10)); // Space before the button panel
      toolPanel.add(buttonPanel);

      // Set the layout of the main panel and add components
      this.setLayout(new BorderLayout());
      this.add(simulationPanel, BorderLayout.CENTER);
      this.add(toolPanel, BorderLayout.EAST);

      // Show the input dialog to collect user input and start the simulation
      showInputDialogAndStartSimulation();
  }

    /**
     * This method shows an info pop up to the user containing information about the application.
     *
     * @param none
     * @return nothing
     */
    private void showAboutDialog() {
        String aboutText = "<html><body>"
                           + "<p style='padding: 10px;'>"
                           + "<strong>Coders:</strong>"
                           + "<ul>"
                           + "<li>Ethan Rivers 1015561</li>"
                           + "<li>Jefferson Gilbert 1140941</li>"
                           + "<li>Daniel Allison 1130809</li>"
                           + "</ul>"
                           + "<strong>Color Legend:</strong>"
                           + "<ul>"
                           + "<li><span style='color: #0000FF;'>●</span> Unvaccinated (Blue)</li>"
                           + "<li><span style='color: #00FFFF;'>●</span> One Shot Vaccinated (Cyan)</li>"
                           + "<li><span style='color: #FFFF00;'>●</span> Two Shots Vaccinated (Yellow)</li>"
                           + "<li><span style='color: #FF00FF;'>●</span> Three Shots Vaccinated (Magenta)</li>"
                           + "<li><span style='color: #008000;'>●</span> Natural Immunity (Green)</li>"
                           + "<li><span style='color: #FF0000;'>●</span> Infected (Red)</li>"
                           + "<li><span style='color: #000000;'>●</span> Dead (Black)</li>"
                           + "</ul>"
                           + "</p></body></html>";
        JOptionPane.showMessageDialog(this, aboutText, "About Pandemic Modeler App", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This method is what generates the initial dialog 
     * prior to the simulation running. The dialog allows
     * for user input based on selected Radio buttons and one slider
     * value. Eliminates the need for user to manual enter values since
     * we just give the predetermined ones within an acceptable range. It
     * also validates that user input is no more or less than 100%. Once
     * the user is submits all valid numbers, the simulation is begun.
     *
     * @param none
     * @return nothing
     */
    private void showInputDialogAndStartSimulation() {
        // Slider for population size
        JSlider populationSlider = new JSlider(100, 1000, lastPopulationSize); // Set to last selected
        populationSlider.setMajorTickSpacing(100);
        populationSlider.setMinorTickSpacing(50);
        populationSlider.setPaintTicks(true);
        populationSlider.setPaintLabels(true);

        // Radio buttons for immunity levels
        ButtonGroup unvaccinatedGroup = new ButtonGroup();
        JRadioButton[] unvaccinatedButtons = createRadioButtons(new String[]{"0%", "25%", "50%", "75%", "100%"}, unvaccinatedGroup);
        unvaccinatedButtons[lastUnvaccinatedPercent / 25].setSelected(true); // Set last selected

        ButtonGroup oneShotGroup = new ButtonGroup();
        JRadioButton[] oneShotButtons = createRadioButtons(new String[]{"0%", "25%", "50%", "75%", "100%"}, oneShotGroup);
        oneShotButtons[lastOneShotPercent / 25].setSelected(true); // Set last selected

        ButtonGroup twoShotGroup = new ButtonGroup();
        JRadioButton[] twoShotButtons = createRadioButtons(new String[]{"0%", "25%", "50%", "75%", "100%"}, twoShotGroup);
        twoShotButtons[lastTwoShotPercent / 25].setSelected(true); // Set last selected

        ButtonGroup threeShotGroup = new ButtonGroup();
        JRadioButton[] threeShotButtons = createRadioButtons(new String[]{"0%", "25%", "50%", "75%", "100%"}, threeShotGroup);
        threeShotButtons[lastThreeShotPercent / 25].setSelected(true); // Set last selected

        ButtonGroup naturalImmunityGroup = new ButtonGroup();
        JRadioButton[] naturalImmunityButtons = createRadioButtons(new String[]{"0%", "25%", "50%", "75%", "100%"}, naturalImmunityGroup);
        naturalImmunityButtons[lastNaturalImmunityPercent / 25].setSelected(true); // Set last selected

        JPanel inputPanel = new JPanel(new GridLayout(6, 2));
        inputPanel.add(new JLabel("Population Size:"));
        inputPanel.add(populationSlider);
        inputPanel.add(new JLabel("Unvaccinated %:"));
        inputPanel.add(createRadioButtonPanel(unvaccinatedButtons));
        inputPanel.add(new JLabel("One Shot %:"));
        inputPanel.add(createRadioButtonPanel(oneShotButtons));
        inputPanel.add(new JLabel("Two Shots %:"));
        inputPanel.add(createRadioButtonPanel(twoShotButtons));
        inputPanel.add(new JLabel("Three Shots %:"));
        inputPanel.add(createRadioButtonPanel(threeShotButtons));
        inputPanel.add(new JLabel("Natural Immunity %:"));
        inputPanel.add(createRadioButtonPanel(naturalImmunityButtons));

        int result = JOptionPane.showConfirmDialog(null, inputPanel, "Enter Simulation Parameters", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            personCount = populationSlider.getValue();
            int unvaccinatedPercent = getSelectedPercentage(unvaccinatedGroup);
            int oneShotPercent = getSelectedPercentage(oneShotGroup);
            int twoShotPercent = getSelectedPercentage(twoShotGroup);
            int threeShotPercent = getSelectedPercentage(threeShotGroup);
            int naturalImmunityPercent = getSelectedPercentage(naturalImmunityGroup);

            if (validateInputs(unvaccinatedPercent, oneShotPercent, twoShotPercent, threeShotPercent, naturalImmunityPercent)) {
                // Store last selected values
                lastPopulationSize = personCount;
                lastUnvaccinatedPercent = unvaccinatedPercent;
                lastOneShotPercent = oneShotPercent;
                lastTwoShotPercent = twoShotPercent;
                lastThreeShotPercent = threeShotPercent;
                lastNaturalImmunityPercent = naturalImmunityPercent;

                initializeSimulation(unvaccinatedPercent, oneShotPercent, twoShotPercent, threeShotPercent, naturalImmunityPercent);
                timer.start();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input values! Ensure total percentages equal 100%.", "Input Error", JOptionPane.ERROR_MESSAGE);
                showInputDialogAndStartSimulation(); // Retry input
            }
        } else {
            //System.exit(0); // Exit if user cancels input
        }
    }

    /**
     * This method generates a group of buttons given assigning labels
     * to them using a seperate array. The purpose here is to avoid redundant
     * typing and we can streamline button creation process for each type of person
     * (unvaccinated, one shot, two shot etc...)
     * 
     *
     * @param labels contains an array of strings representing the text to be applied to each button.
     * @param group is the button group we want to apply all the generated buttons to at the end.
     * @return nothing
     */
    private JRadioButton[] createRadioButtons(String[] labels, ButtonGroup group) {
        JRadioButton[] buttons = new JRadioButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            buttons[i] = new JRadioButton(labels[i]);
            buttons[i].setActionCommand(labels[i].replace("%", ""));
            group.add(buttons[i]);
            if (labels[i].equals("0%")) {
                buttons[i].setSelected(true);
            }
        }
        return buttons;
    }

    /**
     * This method generates a panel and adds previously generated buttons
     * to the panel. To be used sometime after the buttons have been created.
     *
     * @param buttons is an array of previously created buttons that are ready to be presented.
     * @return JPanel a panel that will contain the buttons provided.
     */
    private JPanel createRadioButtonPanel(JRadioButton[] buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JRadioButton button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    /**
     * This method simply gets us the integer value of a 
     * selected button from a ButtonGroup. Since we auto 
     * generated all our buttons to contain labels ranging from
     * 0% up to 100%, this will accurately give us the desired number.
     * To be used prior to simulation start when the input dialog is on screen.
     *
     * @param group the button group which we want to retrieve the selected buttons label from
     * @return percent number
     */
    private int getSelectedPercentage(ButtonGroup group) {
        return Integer.parseInt(group.getSelection().getActionCommand());
    }

    
    /**
     * This method is what starts the simulation based on the previously validated 
     * parameters given by the user. It sets the repaint counter to 0 and creates
     * an array of people objects based on the personCount and percentages given.
     *  
     *
     * @param all the parameters are the percentage values of types of people to be created for the simulation
     * @return void Description of the return value
     */
    private void initializeSimulation(int unvaccinatedPercent, int oneShotPercent, int twoShotPercent, int threeShotPercent, int naturalImmunityPercent) {
        repaintCounter = 0;
        persons = new Person[personCount];
        pauseBtn.setEnabled(true);

        unvaccinatedCount = (int) (personCount * unvaccinatedPercent / 100.0);
        oneShotCount = (int) (personCount * oneShotPercent / 100.0);
        twoShotCount = (int) (personCount * twoShotPercent / 100.0);
        threeShotCount = (int) (personCount * threeShotPercent / 100.0);
        naturalImmunityCount = (int) (personCount * naturalImmunityPercent / 100.0);

        for (int i = 0; i < persons.length; i++) {
            boolean isInfected = (i < 1); // Infect one person initially
            int immunityStatus = 0; // Default to no immunity
            Color color = Color.BLUE; // Default color for uninfected

            if (i < unvaccinatedCount) {
                immunityStatus = 0; // No immunity
                color = Color.BLUE;
            } else if (i < unvaccinatedCount + oneShotCount) {
                immunityStatus = 1; // One shot
                color = Color.CYAN;
            } else if (i < unvaccinatedCount + oneShotCount + twoShotCount) {
                immunityStatus = 2; // Two shots
                color = Color.YELLOW;
            } else if (i < unvaccinatedCount + oneShotCount + twoShotCount + threeShotCount) {
                immunityStatus = 3; // Three shots
                color = Color.MAGENTA;
            } else {
                immunityStatus = 4; // Natural immunity
                color = Color.GREEN;
            }

            if (isInfected) {
                color = Color.RED; // Infected color
                totalInfected++;
            }

            persons[i] = new Person(true, isInfected, immunityStatus, immunityStatus, // Shots = immunity status
                    color, DOT_SIZE, WIDTH, HEIGHT);

            if (isInfected) {
                persons[i].setPreviouslyInfected(true); // Set previously infected flag
            }

            if (immunityStatus != 4)
                persons[i].setHasRecovered(false);
            else
                persons[i].setHasRecovered(true);
        }

        // Reset counters for a new simulation
        totalInfected = 1;
        unvaccinatedInfected = 0;
        oneShotInfected = 0;
        twoShotInfected = 0;
        threeShotInfected = 0;
        naturalImmunityInfected = 0;
        recovered = 0;
        dead = 0;

        // Update initial dashboard values
        updateDashboard();
    }

    /**
     * ensures user input for types of people never exceeds 100% 
     *
     * @return true or false
     */
    private boolean validateInputs(int unvaccinatedPercent, int oneShotPercent, int twoShotPercent, int threeShotPercent, int naturalImmunityPercent) {
        int totalPercentage = unvaccinatedPercent + oneShotPercent + twoShotPercent + threeShotPercent + naturalImmunityPercent;
        return totalPercentage == 100;
    }

    /**
     * generates display panel for tracking real-time simulation statistics.
     *
     * @param nothing
     * @return JPanel that holds the metrics for the current simulation run. 
     */
    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new GridLayout(10, 1)); // Add one more row for the total people

        totalPeopleLabel = new JLabel("Sample Size: 0"); // Initialize total people label
        totalInfectedLabel = new JLabel("Total Infected: 0");
        unvaccinatedInfectedLabel = new JLabel("Unvaccinated Infected: 0");
        oneShotInfectedLabel = new JLabel("One Shot Infected: 0");
        twoShotInfectedLabel = new JLabel("Two Shots Infected: 0");
        threeShotInfectedLabel = new JLabel("Three Shots Infected: 0");
        naturalImmunityInfectedLabel = new JLabel("Natural Immunity Infected: 0");
        recoveredLabel = new JLabel("Recovered: 0");
        deadLabel = new JLabel("Dead: 0");
        dayCounterLabel = new JLabel("Day: 0"); // Initialize day counter

        dashboardPanel.add(totalPeopleLabel); // Add total people to dashboard
        dashboardPanel.add(dayCounterLabel); // Add day counter to dashboard
        dashboardPanel.add(totalInfectedLabel);
        dashboardPanel.add(unvaccinatedInfectedLabel);
        dashboardPanel.add(oneShotInfectedLabel);
        dashboardPanel.add(twoShotInfectedLabel);
        dashboardPanel.add(threeShotInfectedLabel);
        dashboardPanel.add(naturalImmunityInfectedLabel);
        dashboardPanel.add(recoveredLabel);
        dashboardPanel.add(deadLabel);

        return dashboardPanel;
    }

    
    /**
     * pauses the simulation.
     *
     * @param none
     * @return nothing
     */
    private void pauseSimulation() {
        if (persons != null) {
            timer.stop();
            pauseBtn.setEnabled(false);
            resumeBtn.setEnabled(true);
        }
    }

    /**
     * resumes the simulation.
     *
     * @param none
     * @return nothing
     */
    private void resumeSimulation() {
        if (persons != null) {
            timer.start();
            pauseBtn.setEnabled(true);
            resumeBtn.setEnabled(false);
        }
    }

    /**
     * First pauses the simulation and attempts to prompt user for new input parameters
     * for subsequent simulation. If the user submits a valid dialog form, the simulation will restart.
     *
     * @param none
     * @return nothing
     */
    private void restartSimulation() {
        // keep the current state of the simulation just in case user cancels restart
        if (persons != null) {
            pauseSimulation();
            showInputDialogAndStartSimulation();
        } else {
            timer.stop(); // Stop the current simulation
            repaintCounter = 0;
            showInputDialogAndStartSimulation(); // Restart by showing the input dialog again
        }
    }

    
    /**
     * Updates the real-time simulation metrics inside the dash-board panel.
     *
     * @param none
     * @return nothing
     */
    private void updateDashboard() {
        totalPeopleLabel.setText("Sample Size: " + personCount); // Update total people count
        totalInfectedLabel.setText("Total Infected: " + totalInfected);
        unvaccinatedInfectedLabel.setText("Unvaccinated Infected: " + unvaccinatedInfected);
        oneShotInfectedLabel.setText("One Shot Infected: " + oneShotInfected);
        twoShotInfectedLabel.setText("Two Shots Infected: " + twoShotInfected);
        threeShotInfectedLabel.setText("Three Shots Infected: " + threeShotInfected);
        naturalImmunityInfectedLabel.setText("Natural Immunity Infected: " + naturalImmunityInfected);
        recoveredLabel.setText("Recovered: " + recovered);
        deadLabel.setText("Dead: " + dead);

        int days = repaintCounter / (MAX_REPAINTS_ALLOWED / 21); // Calculate the number of days passed based on 21 days for 450 cycles
        dayCounterLabel.setText("Day: " + days); // Update the day counter label
    }

    // Inner class to handle movement
    private class MoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Update positions
            for (Person person : persons) {
                person.move();
            }

            // Check for collisions between all pairs of persons
            for (int i = 0; i < persons.length - 1; i++) {
                for (int j = i + 1; j < persons.length; j++) {
                    persons[i].checkCollision(persons[j]);
                }
            }

            // Update the dashboard with the latest statistics
            updateStatistics();

            if (repaintCounter < MAX_REPAINTS_ALLOWED) {
                simulationPanel.repaint();
                ++repaintCounter;
            } else {
                // Pause when simulation finishes and show final results
                pauseSimulation();
                resumeBtn.setEnabled(false);
                pauseBtn.setEnabled(false);
                showFinalResults();
            }
        }
    }

    
    /**
     * Updates the real-time metrics contained within the dash-board panel
     * for the current simulation.
     *
     * @param none
     * @return none
     */
    private void updateStatistics() {
        // Reset counters for this update cycle directly on the class-level variables
        totalInfected = 0;
        recovered = 0;
        dead = 0;
        unvaccinatedInfected = 0;
        oneShotInfected = 0;
        twoShotInfected = 0;
        threeShotInfected = 0;
        naturalImmunityInfected = 0;
        unvaccinatedDeaths = 0;
        oneShotDeaths = 0;
        twoShotDeaths = 0;
        threeShotDeaths = 0;
        naturalImmunityDeaths = 0;

        // Calculate statistics based on the current state of each person
        for (Person person : persons) {
            if (person.isInfected()) {
                totalInfected++;
                switch (person.getImmunityStatus()) {
                    case 0:
                        unvaccinatedInfected++;
                        break;
                    case 1:
                        oneShotInfected++;
                        break;
                    case 2:
                        twoShotInfected++;
                        break;
                    case 3:
                        threeShotInfected++;
                        break;
                    case 4:
                        naturalImmunityInfected++;
                        break;
                }
            }
            if (person.isRecovered()) {
                recovered++;
            }
            if (!person.isAlive()) {
                dead++;
                switch (person.getImmunityStatus()) {
                    case 0:
                        unvaccinatedDeaths++;
                        break;
                    case 1:
                        oneShotDeaths++;
                        break;
                    case 2:
                        twoShotDeaths++;
                        break;
                    case 3:
                        threeShotDeaths++;
                        break;
                    case 4:
                        naturalImmunityDeaths++;
                        break;
                }
            }
        }

        // Update the dashboard with real-time statistics
        updateDashboard();
    }

    
    /**
     * simply calculates the number for each type of person infected, along
     * with the total people ever infected for the final report of the simulation
     *
     * @param paramName Description of the parameter
     * @return void Description of the return value
     */
    private void calculateGrandTotals() {
        // Reset grand total counters before calculation
        gtInfected = 0;
        gtUnvaccinatedContractions = 0;
        gtOneShotContractions = 0;
        gtTwoShotContractions = 0;
        gtThreeShotContractions = 0;
        gtNaturallyImmuneRecontractions = 0;

        // Loop through each person and update grand totals based on previously infected status
        for (Person person : persons) {
            if (person.PreviouslyInfected()) {
                gtInfected++;
                switch (person.getImmunityStatus()) {
                    case 0:
                        gtUnvaccinatedContractions++;
                        break;
                    case 1:
                        gtOneShotContractions++;
                        break;
                    case 2:
                        gtTwoShotContractions++;
                        break;
                    case 3:
                        gtThreeShotContractions++;
                        break;
                    case 4:
                        // leverage our existing flag to mark them as counted or not
                        if (person.PreviouslyInfected())
                            ++gtNaturallyImmuneRecontractions;
                        break;
                }
            }
        }
    }

    
    /**
     * displays final report with calculated fields.
     *
     * @param none
     * @return nothing
     */
    private void showFinalResults() {
        // Calculate grand totals before showing final results
        calculateGrandTotals();

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("<html><h2>Final Data Presentation</h2>");

        // Calculate and append each percentage to the result string
        resultBuilder.append("<b>1) Percentage of total population that contracted the disease: </b>")
                .append(String.format("%.2f", (double) gtInfected / personCount * 100)).append("%<br>");

        resultBuilder.append("<b>2) Percentage of unvaccinated persons who contracted the disease: </b>")
                .append(unvaccinatedCount > 0 ? String.format("%.2f", (double) gtUnvaccinatedContractions / unvaccinatedCount * 100) : "0.00").append("%<br>");

        resultBuilder.append("<b>3) Percentage of one-shot-vaccinated persons who contracted the disease: </b>")
                .append(oneShotCount > 0 ? String.format("%.2f", (double) gtOneShotContractions / oneShotCount * 100) : "0.00").append("%<br>");

        resultBuilder.append("<b>4) Percentage of two-shot-vaccinated persons who contracted the disease: </b>")
                .append(twoShotCount > 0 ? String.format("%.2f", (double) gtTwoShotContractions / twoShotCount * 100) : "0.00").append("%<br>");

        resultBuilder.append("<b>5) Percentage of three-shot-vaccinated persons who contracted the disease: </b>")
                .append(threeShotCount > 0 ? String.format("%.2f", (double) gtThreeShotContractions / threeShotCount * 100) : "0.00").append("%<br>");

        resultBuilder.append("<b>6) Percentage of naturally immune persons who got re-infected: </b>")
                .append(naturalImmunityCount > 0 ? String.format("%.2f", (double) gtNaturallyImmuneRecontractions / naturalImmunityCount * 100) : "0.00").append("%<br>");

        resultBuilder.append("<b>7) Percentage of those who contracted the disease and recovered: </b>")
                .append((recovered + dead) > 0 ? String.format("%.2f", (double) recovered / (recovered + dead) * 100) : "0.00").append("%<br>");

        resultBuilder.append("<b>8) Death rate percentage by immunity status:</b><ul>");
        resultBuilder.append("<li><b>Unvaccinated: </b>")
                .append(unvaccinatedCount > 0 ? String.format("%.2f", (double) unvaccinatedDeaths / unvaccinatedCount * 100) : "0.00").append("%</li>");
        resultBuilder.append("<li><b>One Shot: </b>")
                .append(oneShotCount > 0 ? String.format("%.2f", (double) oneShotDeaths / oneShotCount * 100) : "0.00").append("%</li>");
        resultBuilder.append("<li><b>Two Shots: </b>")
                .append(twoShotCount > 0 ? String.format("%.2f", (double) twoShotDeaths / twoShotCount * 100) : "0.00").append("%</li>");
        resultBuilder.append("<li><b>Three Shots: </b>")
                .append(threeShotCount > 0 ? String.format("%.2f", (double) threeShotDeaths / threeShotCount * 100) : "0.00").append("%</li>");
        resultBuilder.append("<li><b>Natural Immunity: </b>")
                .append(naturalImmunityCount > 0 ? String.format("%.2f", (double) naturalImmunityDeaths / naturalImmunityCount * 100) : "0.00").append("%</li>");
        resultBuilder.append("</ul></html>");

        // Display the results in a dialog
        JOptionPane.showMessageDialog(this, resultBuilder.toString(), "Final Results", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Create a JFrame to hold the JPanel
        JFrame frame = new JFrame("Pandemic Modeler Simulation");

        // Boilerplate
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        PandemicModelerApp pandemicModelerApp = new PandemicModelerApp();
        frame.add(pandemicModelerApp);

        frame.pack();
        frame.setVisible(true);
    }
    // End main
}
// End class
