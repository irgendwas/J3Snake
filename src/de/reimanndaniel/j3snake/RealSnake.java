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

import de.reimanndaniel.ledcube.system.LEDCube;
import de.reimanndaniel.ledcube.system.LEDCubePoint;
import de.reimanndaniel.ledcube.system.LEDCubeShape;
import de.reimanndaniel.ledcube.util.LEDCubePointFactory;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *  A playable snake.
 *
 *  @author Daniel Reimann <coding@reimanndaniel.de>
 *  @version 0.9.0
 *  @since 0.9.0
 */
public class RealSnake extends LinkedList<LEDCubePoint> implements LEDCubeShape {

    /**
     *  The current direction of the snake.
     */
    Direction dir[];
    /**
     *  Has the snake bitten itself?
     */
    boolean bit;
    /**
     *  Possible directions of the snake.
     */
    public enum Direction {

        /**
         *  Direction of snake heading up.
         */
        Up( 0, 1, 0, "Up" ),
        /**
         *  Direction of snake heading down.
         */
        Down( 0, -1, 0, "Down" ),
        /**
         *  Direction of snake heading left.
         */
        Left( -1, 0, 0, "Left" ),
        /**
         *  Direction of snake heading right.
         */
        Right( 1, 0, 0, "Right" ),
        /**
         *  Direction of snake heading ahead.
         */
        Ahead( 0, 0, -1, "Ahead" ),
        /**
         *  Direction of snake heading back.
         */
        Back( 0, 0, 1, "Back" ),
        /**
         *  Direction of snake heading nowhere. Just to avoid null.
         */
        Nowhere( 0, 0, 0, "Nowhere" );

        /**
         *  X coordinate
         */
        private int x;
        /**
         *  Y coordinate
         */
        private int y;
        /**
         *  Z coordinate
         */
        private int z;
        /**
         *  The name for toString().
         */
        private String name;

        /**
         *  Constructor.
         *
         *  @param x X coordinate
         *  @param y Y coordinate
         *  @param z Z coordinate
         *  @param name the name of the direction
         */
        private Direction( int x, int y, int z, String name ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = name;
        }

        /**
         *  @return X coordinate
         */
        public int getX() {
            return x;
        }

        /**
         *  @return Y coordinate
         */
        public int getY() {
            return y;
        }

        /**
         *  @return Z coordinate
         */
        public int getZ() {
            return z;
        }

        /**
         *  Gets a direction by an ID.
         *
         *  @param id the id of the direction
         *  @return the direction
         */
        public static Direction fromID( int id ) {
            switch( id ) {
                case 6:
                    return Back;
                case 5:
                    return Ahead;
                case 4:
                    return Right;
                case 3:
                    return Left;
                case 2:
                    return Down;
                case 1:
                    return Up;
            }
            return Nowhere;
        }

        /**
         *  Gets a direction by the corresponding coordinates.
         *
         *  @param x X coordinate
         *  @param y Y coordinate
         *  @param z Z coordinate
         *  @return the direction with this coordinates
         */
        public static Direction fromCoord( int x, int y, int z ) {
            switch( y ) {
                case 1:
                    return Up;
                case -1:
                    return Down;
            }
            switch( x ) {
                case 1:
                    return Right;
                case -1:
                    return Left;
            }
            switch( z ) {
                case 1:
                    return Back;
                case -1:
                    return Ahead;
            }
            return Nowhere;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    /**
     *  Creates a snake in the game.
     *
     *  @param cube the LED cube of the snake
     */
    public RealSnake( LEDCube cube ) {
        bit = false;
        int direction = (int) ( Math.random() * 4 + 3 );
        this.dir = new Direction[] { Direction.fromID( direction ), Direction.Nowhere };
        LEDCubePoint head = LEDCubePointFactory.random( cube.getDimension(), 2 );
        add( head );
        add( new LEDCubePoint( head.getX() - dir[0].getX(), head.getY() - dir[0].getY(), head.getZ() - dir[0].getZ() ) );
        add( new LEDCubePoint( head.getX() - ( 2 * dir[0].getX() ), head.getY() - ( 2 * dir[0].getY() ), head.getZ() - ( 2 * dir[0].getZ() ) ) );
    }

    /**
     *  @return the next point of the head of the snake
     */
    public LEDCubePoint getNext() {
        return new LEDCubePoint( get( 0 ).getX() + dir[0].getX(), get( 0 ).getY() + dir[0].getY(), get( 0 ).getZ() + dir[0].getZ() );
    }

    /**
     *  Move the snake one step forward.
     *
     *  @return the new point of the head and the tail of the snake
     */
    public LEDCubePoint[] move() {
        return move( false );
    }

    /**
     *  Move the snake one step forward and eat a fruit or not.
     *
     *  @param eat did the snake eat a fruit?
     *  @return the new point of the head of the snake
     */
    public LEDCubePoint[] move( boolean eat ) {
        LEDCubePoint[] bothends = new LEDCubePoint[] { getNext(), null };
        if( contains( bothends[0] ) ) bit = true;
        listIterator().add( bothends[0] );
        if( !eat ) {
            // optimizable (?)
            Iterator<LEDCubePoint> it = descendingIterator();
            bothends[1] = it.next();
            it.remove();
        }
        return bothends;
    }

    /**
     *  @return whether the snake has bitten in its tail
     */
    public boolean bit() {
        return bit;
    }

    /**
     *  Change the direction with a 2D direction.
     *
     *  @param dir 2D direction (Up, Down, Left, Right)
     */
    public void change2D( Direction direct ) {
        // magic (maybe sometime better commented)
        int x =
                dir[0].getZ() * direct.getX() * -1 +
                dir[1].getZ() * Math.abs( dir[0].getY() ) * direct.getX() * -1 +
                dir[1].getX() * dir[0].getY() * direct.getY() * -1;
        int y = dir[0].getX() != 0 || dir[0].getZ() != 0 ? direct.getY() : 0;
        int z = dir[0].getX() * direct.getX() +
                dir[1].getX() * Math.abs( dir[0].getY() ) * direct.getX() +
                dir[1].getZ() * dir[0].getY() * direct.getY() * -1;
        dir[1] = dir[0];
        dir[0] = Direction.fromCoord( x, y, z );
    }

    /**
     *  Change the direction with a 3D direction.
     *
     *  @param dir 3D direction
     */
    public void change3D( Direction direct ) {
        this.dir[0] = direct;
    }

}
