package ru.ppzh.senegalcheckers;

import java.util.ArrayList;

public class Moves {
	private int depth;
	private int n;		// Amount of moves from this node.
	private int i;
    private int j;
	private ArrayList<Point> point;     // Coordinates of the next moves.
	private ArrayList<Moves> next;
    
	public Moves() {
		n = 0;
		depth = i = j = -1;
		point = new ArrayList<>();
		next = new ArrayList<>();
	}

	public void increaseN(){
		n++;
	}
	
	public void decreaseN(){
		n--;
	}
	
	public void reset(){
		n = 0;
		depth = i = j = -1;
		point.clear();
		next.clear();
	}
	
	public int getN() {
		return n;
	}

	public ArrayList<Point> getPoint() {
		return point;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public int getJ() {
		return j;
	}

	public void setJ(int j) {
		this.j = j;
	}

	public ArrayList<Moves> getNext() {
		return next;
	}	
	
}
