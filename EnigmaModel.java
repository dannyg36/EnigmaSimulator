/*
 * File: EnigmaModel.java
 * ----------------------
 * This class defines the starter version of the EnigmaModel class,
 * which doesn't implement any of the methods.
 */

package edu.willamette.cs1.enigma;

import static edu.willamette.cs1.enigma.EnigmaConstants.*;

import java.util.ArrayList;
import java.util.HashMap;

public class EnigmaModel {

    public EnigmaModel() {
        views = new ArrayList<EnigmaView>();
    }

/**
 * Adds a view to this model.
 *
 * @param view The view being added
 */

    public void addView(EnigmaView view) {
        views.add(view);
    }

/**
 * Sends an update request to all the views.
 */

    public void update() {
        for (EnigmaView view : views) {
            view.update();
        }
    }

/**
 * Returns true if the specified letter key is pressed.
 *
 * @param letter The letter key being tested as a one-character string.
 */

    private HashMap<String, Boolean> keyStates = new HashMap<>();

    public boolean isKeyDown(String letter) {
        return keyStates.getOrDefault(letter, false);
    }

/**
 * Returns true if the specified lamp is lit.
 *
 * @param letter The lamp being tested as a one-character string.
 */

    private HashMap<String, Boolean> lampStates = new HashMap<>();

    public boolean isLampOn(String letter) {
        return lampStates.getOrDefault(letter, false);
    }

/**
 * Returns the letter visible through the rotor at the specified inded.
 *
 * @param index The index of the rotor (0-2)
 * @return The letter visible in the indicated rotor
 */

    private int[] rotorPositions = new int[N_ROTORS];
    private String currentLamp = null;

    public String getRotorLetter(int index) {
        return String.valueOf(ALPHABET.charAt(rotorPositions[index]));
    }

/**
 * Called automatically by the view when the specified key is pressed.
 *
 * @param key The key the user pressed as a one-character string
 */

    public void keyPressed(String key) {
        keyStates.put(key, true);
        advanceRotors();
        currentLamp = encryptLetter(key);
        if (currentLamp != null) {
            lampStates.put(currentLamp, true);
        }
        update();
    }

/**
 * Called automatically by the view when the specified key is released.
 *
 * @param key The key the user released as a one-character string
 */

    public void keyReleased(String key) {
        keyStates.put(key, false);
        if (currentLamp != null) {
            lampStates.put(currentLamp, false);
            currentLamp = null;
        }
        update();
    }

/**
 * Called automatically by the view when the rotor at the specified
 * index (0-2) is clicked.
 *
 * @param index The index of the rotor that was clicked
 */

    public void rotorClicked(int index) {
        rotorPositions[index] = (rotorPositions[index] + 1) % 26;
        update();
    }

    private void advanceRotors() {
        // Fast rotor always advances
        rotorPositions[2] = (rotorPositions[2] + 1) % 26;
        
        // Medium rotor advances when fast rotor completes a revolution
        if (rotorPositions[2] == 0) {
            rotorPositions[1] = (rotorPositions[1] + 1) % 26;
            
            // Slow rotor advances when medium rotor completes a revolution
            if (rotorPositions[1] == 0) {
                rotorPositions[0] = (rotorPositions[0] + 1) % 26;
            }
        }
    }

    private String encryptLetter(String letter) {
        int pos = ALPHABET.indexOf(letter);
        if (pos == -1) return null;

        // Forward through rotors
        for (int i = N_ROTORS - 1; i >= 0; i--) {
            pos = (pos + rotorPositions[i]) % 26;
            pos = ROTOR_PERMUTATIONS[i].indexOf(ALPHABET.charAt(pos));
        }

        // Through reflector
        pos = REFLECTOR_PERMUTATION.indexOf(ALPHABET.charAt(pos));

        // Backward through rotors
        for (int i = 0; i < N_ROTORS; i++) {
            pos = ALPHABET.indexOf(ROTOR_PERMUTATIONS[i].charAt(pos));
            pos = (pos - rotorPositions[i] + 26) % 26;
        }

        return String.valueOf(ALPHABET.charAt(pos));
    }

/* Main program */

    public static void main(String[] args) {
        EnigmaModel model = new EnigmaModel();
        EnigmaView view = new EnigmaView(model);
        model.addView(view);
    }

/* Private instance variables */

    private ArrayList<EnigmaView> views;
    
}
