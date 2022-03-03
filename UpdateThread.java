/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.eksamen;

import static com.mycompany.eksamen.App.fåAlleAktiveRom;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;

/**
 *
 * @author alexa
 */
public class UpdateThread implements Runnable{

    private int operasjon;
    
    public UpdateThread(int operasjon) throws IOException{
        this.operasjon = operasjon;
        
    }
    
    @Override
    public void run() {
        while(true){
            
            switch(operasjon){
                case 3:
                
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                fåAlleAktiveRom();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            } catch (ClassNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    
                    break;

                case 5:
                
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    if(App.iRom != 0){
                    
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    
                                   App.tegnChat();
                                   App.fåAlleBrukereIrom();
                                    
                                } catch (ClassNotFoundException ex) {
                                    ex.printStackTrace();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                    break;

            }
            
        }
    }
    
}
