/**
 * Program Name: Person.java
 * Purpose: This class represents a person object.
 * Coders: Ethan Rivers 1015561, Jefferson Gilbert 1140941 & Daniel Allison 1130809
 * Date: Aug 1, 2024
 */
import java.awt.Color;
import java.util.*;

public class Person {
    private boolean isAlive;
    private boolean isInfected;
    private boolean hasRecovered;
    private boolean previouslyInfected;
		private int immunityStatus;
    private int numberOfShots;
    private Color color;
    
    private int dotDiameter;
    private int xCoordinate;
    private int yCoordinate;
    private int xIncrementValue; // number of pixels the object will move each drawing cycle -5 to +5
    private int yIncrementValue; // number of pixels the object will move each drawing cycle -5 to +5
    
    private int cycleCounter; // tracks how long they stay infected. 0-150 (only starts when isInfected = true)...
    private static int deathCounter = 0; // tracks the number of deaths
    
    private int screenWidth; // width boundary of the moving area
    private int screenHeight; // height boundary of the moving area

    public Person(boolean isAlive, boolean isInfected, int immunityStatus, int numberOfShots, Color color,
                  int dotDiameter, int screenWidth, int screenHeight) {
        this.isAlive = isAlive;
        this.isInfected = isInfected;
        this.immunityStatus = immunityStatus;
        this.numberOfShots = numberOfShots;
        this.cycleCounter = 0;
        this.color = color;
        this.dotDiameter = dotDiameter;
        
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        
        // ALL THE LOGIC BELOW IS HARVESTED DIRECTCLY FROM BOUNCER DEMO 
        
        // generate random starting position for x coordinate
        int randomX, randomY;
        boolean generateCoordinateFlag = true;
     
        while (generateCoordinateFlag) {
            // generate a random value using widthValue
            randomX = (int) (Math.random() * screenWidth);
            if (randomX >= 0 && randomX <= screenWidth - this.dotDiameter) {
                // we have a valid x value, assign it to xCoord
                this.xCoordinate = randomX;
                generateCoordinateFlag = false;
            }
        }
        
        // generate a random starting position for y coordinate
        generateCoordinateFlag = true;
        while (generateCoordinateFlag) {
            // repeat for yCoord
            randomY = (int) (Math.random() * screenHeight);
            if (randomY >= 0 && randomY <= screenHeight - this.dotDiameter) {
                // we have a valid y value, assign it to yCoord
                this.yCoordinate = randomY;
                generateCoordinateFlag = false;
            }
        }
        
        // generate increment values
        boolean generateIncrementsFlag = true;
        while (generateIncrementsFlag) {
            this.xIncrementValue = (int) (Math.random() * 11) - 5; // random number between -5 and 5
            this.yIncrementValue = (int) (Math.random() * 11) - 5; // random number between -5 and 5
            if (this.xIncrementValue == 0 && this.yIncrementValue == 0) {
                // run it again
                this.xIncrementValue = (int) (Math.random() * 11 - 5);
                this.yIncrementValue = (int) (Math.random() * 11 - 5);
            } else {
                generateIncrementsFlag = false;
            }
        } // end while
    } // end ctor

    
    /**
     * moves the dot around the screen based on the increment values. 
     * 
     * @note harvested from bouncer demo
     * @note this function takes into account the line border width used in the view for movement.
     * @param paramName Description of the parameter
     * @return void Description of the return value
     */
    public void move() {
      if (!isAlive) {
          return; // Do nothing if the person is not alive
      }

      int borderWidth = 5; // based on the line border width assigned in PandemicModelerApp

      // Check if near boundary. If so, reverse the direction
      if (xCoordinate >= screenWidth - dotDiameter - borderWidth) {
          // We are at the right side, so reverse x direction
          xIncrementValue = -Math.abs(xIncrementValue); // Ensure it moves left
      }
      if (xCoordinate <= borderWidth) {
          // We are at the left side, so reverse x direction
          xIncrementValue = Math.abs(xIncrementValue); // Ensure it moves right
      }
      if (yCoordinate >= screenHeight - dotDiameter - borderWidth) {
          // We are at the bottom, so reverse y direction
          yIncrementValue = -Math.abs(yIncrementValue); // Ensure it moves up
      }
      if (yCoordinate <= borderWidth) {
          // We are at the top, so reverse y direction
          yIncrementValue = Math.abs(yIncrementValue); // Ensure it moves down
      }

      // Adjust the position using the increment values
      xCoordinate += xIncrementValue;
      yCoordinate += yIncrementValue;

      // Increment cycleCounter if infected and check if the person dies or recovers
      if (isInfected) {
          cycleCounter++;

          if (cycleCounter >= 150) {
              // Check if the person dies
              if (checkDeath()) {
                  // Person dies
                  isAlive = false;
                  isInfected = false;
                  color = Color.BLACK;
                  xIncrementValue = 0;
                  yIncrementValue = 0;
                  deathCounter++;
              } else {
                  // Person recovers
                  isInfected = false;
                  color = Color.GREEN; // Recovered with mild natural immunity
                  hasRecovered = true;
              }
          }
      }
  }

    
    /**
     * checks if person died using percentages based on their immunization status 
     *
     * @param none
     * @return true or false
     */
    private boolean checkDeath() {
        Random rand = new Random();
        switch (immunityStatus) {
            case 0: // No immunity, 10% chance of death
                return rand.nextDouble() < 0.1;
            case 1: // One shot, 7% chance of death
                return rand.nextDouble() < 0.07;
            case 2: // Two shots, 3% chance of death
                return rand.nextDouble() < 0.03;
            case 3: // Booster shot, 1% chance of death
                return rand.nextDouble() < 0.01;
            case 4: // Natural immunity, 3% chance of death
                return rand.nextDouble() < 0.03;
            default:
                return false;
        }
    }

    /**
     * checks if two people occupy the same space in time. (they touched)
     *
     * @param other the person who we want to compare against
     * @return nothing
     * @note harvested from bouncer demo
     */
    public void checkCollision(Person other) {
        if (!this.isAlive || !other.isAlive) {
            return; // No interaction if either person is not alive
        }

        int deltaX = this.xCoordinate - other.xCoordinate;
        int deltaY = this.yCoordinate - other.yCoordinate;

        // Check if the distance is less than or equal to the dot diameter (collision)
        if (Math.sqrt(deltaX * deltaX + deltaY * deltaY) <= dotDiameter) {
            // Reverse directions of both persons
            this.xIncrementValue *= -1;
            this.yIncrementValue *= -1;
            other.xIncrementValue *= -1;
            other.yIncrementValue *= -1;

            // Generate a new set of random values for the xIncrement and yIncrement
            int firstPersonNewXIncrement = (int) (Math.random() * 11) - 5;
            int firstPersonNewYIncrement = (int) (Math.random() * 11) - 5;
            int secondPersonNewXIncrement = (int) (Math.random() * 11) - 5;
            int secondPersonNewYIncrement = (int) (Math.random() * 11) - 5;

            // Apply new increments
            this.xIncrementValue = firstPersonNewXIncrement;
            this.yIncrementValue = firstPersonNewYIncrement;
            other.xIncrementValue = secondPersonNewXIncrement;
            other.yIncrementValue = secondPersonNewYIncrement;

            // Change color and infection status upon collision
            if (this.isInfected != other.isInfected) {
                // Attempt to infect the non-infected person
                if (this.isInfected) {
                    attemptInfection(other);
                } else {
                    other.attemptInfection(this);
                }
            }
        }
    }

    
    /**
     * Tries to infect another person. This method is to be used when two people collide if one is
     * infected already and the other is not.
     *
     * @param paramName Description of the parameter
     * @return void Description of the return value
     */
    public void attemptInfection(Person other) {
        if (!other.isInfected && other.isAlive) { // Ensure the other person is alive and not infected
            Random rand = new Random();

            switch (other.immunityStatus) {
                case 0: // No immunity, 80% chance of infection
                    if (rand.nextDouble() < 0.8) {
                        other.setInfected(true);
                        other.setColor(Color.RED);
                        other.setHasRecovered(false); // Reset recovery status on infection
                        if(!other.previouslyInfected)
                        	other.setPreviouslyInfected(true);
                    }
                    break;
                case 1: // One shot, 60% chance of infection
                    if (rand.nextDouble() < 0.6) {
                        other.setInfected(true);
                        other.setColor(Color.RED);
                        other.setHasRecovered(false); // Reset recovery status on infection
                        if(!other.previouslyInfected)
                        	other.setPreviouslyInfected(true);
                    }
                    break;
                case 2: // Two shots, 30% chance of infection
                    if (rand.nextDouble() < 0.3) {
                        other.setInfected(true);
                        other.setColor(Color.RED);
                        other.setHasRecovered(false); // Reset recovery status on infection
                        if(!other.previouslyInfected)
                        	other.setPreviouslyInfected(true);
                    }
                    break;
                case 3: // Booster shot, 10% chance of infection
                    if (rand.nextDouble() < 0.1) {
                        other.setInfected(true);
                        other.setColor(Color.RED);
                        other.setHasRecovered(false); // Reset recovery status on infection
                        if(!other.previouslyInfected)
                        	other.setPreviouslyInfected(true);
                    }
                    break;
                case 4: // Natural immunity, 40% chance of infection
                    if (rand.nextDouble() < 0.4) {
                        other.setInfected(true);
                        other.setColor(Color.RED);
                        other.setHasRecovered(false); // Reset recovery status on infection
                        if(!other.previouslyInfected)
                        	other.setPreviouslyInfected(true);
                    }
                    break;
                default:
                    break;
            }
        }
    }
    
    
    /**
     * Added this to help us track grand totals. Lets us know if the person has been sick 
     * at least once before.
     *
     * @param none
     * @return true or false
     */
    public boolean PreviouslyInfected()
		{
			return previouslyInfected;
		}

		public void setPreviouslyInfected(boolean previouslyInfected)
		{
			this.previouslyInfected = previouslyInfected;
		}

		
    /**
     * Lets us know if the person recovered form a sickness before. Again, to be used
     * in calculating final outputs ideally.
     *
     * @param paramName Description of the parameter
     * @return boolean Description of the return value
     */
    public boolean isRecovered()
		{
			return hasRecovered;
		}

		public void setHasRecovered(boolean hasRecovered)
		{
			this.hasRecovered = hasRecovered;
		}

		public int getDotDiameter() {
        return dotDiameter;
    }

    public void setDotDiameter(int dotDiameter) {
        this.dotDiameter = dotDiameter;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public boolean isInfected() {
        return isInfected;
    }

    public void setInfected(boolean isInfected) {
        this.isInfected = isInfected;
    }

    public int getImmunityStatus() {
        return immunityStatus;
    }

    public void setImmunityStatus(int immunityStatus) {
        this.immunityStatus = immunityStatus;
    }

    public int getNumberOfShots() {
        return numberOfShots;
    }

    public void setNumberOfShots(int numberOfShots) {
        this.numberOfShots = numberOfShots;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public int getxIncrementValue() {
        return xIncrementValue;
    }

    public void setxIncrementValue(int xIncrementValue) {
        this.xIncrementValue = xIncrementValue;
    }

    public int getyIncrementValue() {
        return yIncrementValue;
    }

    public void setyIncrementValue(int yIncrementValue) {
        this.yIncrementValue = yIncrementValue;
    }

    public int getCycleCounter() {
        return cycleCounter;
    }

    public void setCycleCounter(int cycleCounter) {
        this.cycleCounter = cycleCounter;
    }

    public static int getDeathCounter() {
        return deathCounter;
    }
}
// end class
