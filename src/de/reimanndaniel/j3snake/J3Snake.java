/*
 *  Copyright 2012, 2013 Daniel Reimann
 *
 *  This file is part of J3Snake.
 *
 *  J3Snake is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  J3Snake is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with J3Snake.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.reimanndaniel.j3snake;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.system.AppSettings;
import de.reimanndaniel.ledcube.demo.LEDCubeCamera;
import de.reimanndaniel.ledcube.system.LEDCube;
import de.reimanndaniel.ledcube.system.LEDCubeDimension;
import de.reimanndaniel.ledcube.system.LEDCubePoint;
import de.reimanndaniel.ledcube.system.LEDCubeViewer;
import de.reimanndaniel.ledcube.util.LEDCubePointFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *  The jme3 Application of J3Snake.
 *
 *  @author Daniel Reimann <coding@reimanndaniel.de>
 *  @version 0.9.0
 *  @since 0.9.0
 */
public class J3Snake extends SimpleApplication implements ActionListener {

    /**
     *  Pauses the game.
     */
    private boolean pause;
    /**
     *  Is true when no game is currently playing.
     */
    private boolean over;
    /**
     *  Time of last action.
     */
    private long timesec;
    /**
     *  The amount of players playing the game.
     */
    private int playerCount;
    /**
     *  Holds the snakes of all players.
     */
    private List<RealSnake> snakes;
    /**
     *  The position of the fruit.
     */
    private LEDCubePoint fruit;
    /**
     *  The color of an off turned LED.
     */
    private ColorRGBA off;
    /**
     *  The LED cube.
     */
    private LEDCube cube;

    /**
     *  Starts the J3Snake game.
     *
     *  @param args command line arguments
     */
    public static void main( String[] args ) {
        J3Snake app = new J3Snake();
        AppSettings settings = new AppSettings( true );
        settings.setTitle( "J3Snake Beta" );
        settings.setResolution( 800, 600 );
        app.setSettings( settings );
        app.setShowSettings( false );
        app.start();
    }

    /**
     *  Initiates all beginning parameters.
     */
    @Override
    public void simpleInitApp() {
        off = new ColorRGBA( 16/255f, 16/255f, 16/255f, 128/255f );

        // view
        LEDCubeDimension dim = new LEDCubeDimension( 8 );
        cube = new LEDCube( dim, off );
        LEDCubeViewer viewer = new LEDCubeViewer( cube, assetManager );
        viewer.paint();

        // post processor bloom
        FilterPostProcessor fpp = new FilterPostProcessor( assetManager );
        BloomFilter bloom = new BloomFilter( BloomFilter.GlowMode.Objects );
        fpp.addFilter( bloom );
        viewPort.addProcessor( fpp );

        // for the variable view
        flyCam.setEnabled( false );
        LEDCubeCamera chaser = new LEDCubeCamera( cam, viewer );
        chaser.registerWithSpecialInput( inputManager );
        inputManager.setCursorVisible( false );

        // currently hardcoded
        playerCount = 1;
        // controls
        inputManager.addMapping( "Pause", new KeyTrigger( KeyInput.KEY_P ) );
        inputManager.addMapping( "NewGame", new KeyTrigger( KeyInput.KEY_N ) );
        inputManager.addListener( this, new String[]{ "Pause", "NewGame" } );
        if( playerCount >= 1 ) {
            String controls[] = {
                "Player1Up",
                "Player1Down",
                "Player1Left",
                "Player1Right"
            };
            inputManager.addMapping( controls[0], new KeyTrigger( KeyInput.KEY_UP ) );
            inputManager.addMapping( controls[1], new KeyTrigger( KeyInput.KEY_DOWN ) );
            inputManager.addMapping( controls[2], new KeyTrigger( KeyInput.KEY_LEFT ) );
            inputManager.addMapping( controls[3], new KeyTrigger( KeyInput.KEY_RIGHT ) );
            inputManager.addListener( this, controls );
        }

        // make it visible
        rootNode.attachChild( viewer );
        newGame();
    }

    /**
     *  Starts a new game.
     */
    private void newGame() {
        over = true;
        cube.fill( off );

        snakes = new ArrayList<RealSnake>( playerCount );
        if( playerCount >= 1 ) {
            RealSnake snake = new RealSnake( cube );
            snakes.add( snake );
            for( LEDCubePoint point: snake ) {
                cube.switchOn( point );
            }
        }

        timesec = System.currentTimeMillis();
        setFruit();
        pause = false;
        over = false;
    }

    /**
     *  Updates the game state.
     *
     *  @param tpf passed time
     */
    @Override
    public void simpleUpdate( float tpf ) {
        if( !pause && !over ) {
            long curtime = System.currentTimeMillis();
            if( curtime - timesec > 1500 ) {
                for( RealSnake snake: snakes ) {
                    boolean eat = snake.getNext().equals( fruit );
                    if( eat ) setFruit();
                    LEDCubePoint[] bothends = snake.move( eat );
                    LEDCubePoint head = bothends[0];
                    LEDCubePoint tail = bothends[1];
                    if( !cube.getDimension().in( head ) || snake.bit() ) {
                        over = true;
                    }
                    else {
                        cube.switchOn( head );
                        if( tail != null && !tail.equals( fruit ) ) {
                            cube.setColor( tail, off );
                        }
                    }
                }
                timesec = System.currentTimeMillis();
            }
        }
    }

    /**
     *  Reacts on the player input.
     *
     *  @param name action name
     *  @param isPressed whether it is still pressed
     *  @param tpf the passed time
     */
    public void onAction( String name, boolean isPressed, float tpf ) {
        if( !isPressed ) {
            if( name.equals( "Player1Up" ) ) {
                snakes.get( 0 ).change2D( RealSnake.Direction.Up );
            }
            else if( name.equals( "Player1Down" ) ) {
                snakes.get( 0 ).change2D( RealSnake.Direction.Down );
            }
            else if( name.equals( "Player1Left" ) ) {
                snakes.get( 0 ).change2D( RealSnake.Direction.Left );
            }
            else if( name.equals( "Player1Right" ) ) {
                snakes.get( 0 ).change2D( RealSnake.Direction.Right );
            }
            else if( name.equals( "Pause" ) ) {
                pause = !pause;
            }
            else if( name.equals( "NewGame" ) ) {
                newGame();
            }
        }
    }

    /**
     *  Sets a new fruit.
     */
    private void setFruit() {
        fruit = LEDCubePointFactory.random( cube.getDimension() );
        cube.switchOn( fruit );
    }

}
