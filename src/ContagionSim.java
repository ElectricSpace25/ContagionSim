import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class ContagionSim extends PApplet {
    int boardWidth = 800;
    int boardHeight = 400;
    int partitionHeight = 50;

    int radius = 5;
    boolean testing = false;

    int population = 50;            //How many people are on the board
    int infectedStart = 5;          //How many people start infected
    float boost = 1;                //Speed boost (if too fast, bouncing will break)
    int recoveryRate = 1000;        //How many frames before someone recovers
    int infectionChance = 90;       //Chance out of 100 for someone to get infected when they encounter an infected person (no masks)
    int maskChance = 50;            //Chance of of 100 that someone is wearing a mask
    double infMaskMultiplier = .8;  //Effectiveness of a mask if the infected person is wearing it (higher = more effective)
    double maskMultiplier = .2;     //Effectiveness of a mask if the uninfected person is wearing it (higher = more effective)
    int deathChance = 1;           //Chance out of 100 of someone dying after infection

    ArrayList<Person> people = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main("ContagionSim", args);
    }

    public class Person {
        // 1) infection status 0 = uninfected, 1 = infected, 2 = recovered, 3 = deceased
        int status;
        int framesInfected = 0;
        boolean mask = false;

        PVector position;
        PVector velocity;

        public Person(int status, PVector position, PVector velocity) {
            this.status = status;
            this.position = position;
            this.velocity = velocity;
        }
    }

    public void setup() {

        //min + Math.random() * (max - min)

        if (!testing) {
            for (int i = 0; i < population; i++) {

                //Random position
                float x = (float) (radius + Math.random() * (boardWidth - radius - radius));
                float y = (float) (radius + Math.random() * (boardHeight - radius - radius));

                //Random velocity
                PVector velocity = PVector.random2D();
                velocity.x *= boost;
                velocity.y *= boost;
                people.add(new Person(0, new PVector(x, y), velocity));

                //Random mask
                if (randomChance(maskChance)) {
                    people.get(i).mask = true;
                }
            }

            //Initial infection
            for (int i = 0; i < infectedStart; i++) {
                people.get(i).status = 1;
            }
        }

        /* Testing */
        if (testing) {
            Person p1 = new Person(1, new PVector(400, 300), new PVector(0, 3));
            Person p2 = new Person(0, new PVector(400, 100), new PVector(0, -3));
            p1.mask = true;
            p2.mask = true;
            people.add(p1);
            people.add(p2);
        }

    }

    public void settings() {
        size(boardWidth, boardHeight + partitionHeight);
    }

    public void draw() {
        background(255); //
        fill(0);
        for (Person p1 : people) {

            //Recovery and death
            if (p1.status == 1) p1.framesInfected++;
            if (p1.framesInfected == recoveryRate && p1.status == 1) {
                if (randomChance(deathChance)) {
                    p1.status = 3;
                } else p1.status = 2;
            }

            //Wall bounce
            if (p1.position.x >= boardWidth - radius && p1.velocity.x > 0) { //Right wall
                p1.velocity.x *= -1;
            }

            if (p1.position.x <= radius && p1.velocity.x < 0) { //Left wall
                p1.velocity.x *= -1;
            }

            if (p1.position.y >= boardHeight - radius && p1.velocity.y > 0) { //Ceiling
                p1.velocity.y *= -1;
            }

            if (p1.position.y <= radius && p1.velocity.y < 0) { //Floor
                p1.velocity.y *= -1;
            }

            //Person bounce
            for (int i = 0; i < people.size(); i++) {
                if (i > people.indexOf(p1)) {
                    Person p2 = people.get(i);

                    //Check distance
                    if (PVector.dist(p1.position, p2.position) <= radius * 2) {
                        if (testing) print("touch ");

                        //Infect
                        if ((p1.status == 1 && p2.status == 0) || (p1.status == 0 && p2.status == 1)) {
                            double multiplier = 1;
                            if (p1.mask) {
                                if (p1.status == 1) multiplier *= (1 - infMaskMultiplier);
                                else multiplier *= (1 - maskMultiplier);
                            }
                            if (p2.mask) {
                                if (p2.status == 1) multiplier *= (1 - infMaskMultiplier);
                                else multiplier *= (1 - maskMultiplier);
                            }
                            if (randomChance((int) (infectionChance * multiplier))) {
                                if (testing) print("infect ");
                                p1.status = 1;
                                p2.status = 1;
                            }
                        }

                        //Swap velocities
                        PVector temp = p1.velocity;
                        p1.velocity = p2.velocity;
                        p2.velocity = temp;


                        //Unstick
//                        if (PVector.dist(p1.position, p2.position) < radius * 2) {
//                            float nudgeDistance = ((radius * 2) - PVector.dist(p1.position, p2.position)) / 2;
//
//                            PVector midPoint = PVector.lerp(p1.position, p2.position, 0.5f);
//
//                            PVector line = PVector.sub(p1.position, midPoint);
//
//                            float scaleFactor = (line.mag() + nudgeDistance) / line.mag();
//
//                            p1.position = midPoint.copy().add(line.copy().mult(scaleFactor));
//                            p2.position = midPoint.sub(temp.mult(scaleFactor));
//                        }
                    }
                }
            }
        }

        int healthy = 0;
        int infected = 0;
        int recovered = 0;
        int deceased = 0;
        strokeWeight(2);

        //Draw people
        for (Person p : people) {
            p.position.add(p.velocity);
            if (p.status == 0) {
                fill(0, 255, 0);
                stroke(0, 255, 0);
                healthy++;
            }
            if (p.status == 1) {
                fill(255, 0, 0);
                stroke(255, 0, 0);
                infected++;
            }
            if (p.status == 2) {
                fill(0, 0, 255);
                stroke(0, 0, 255);
                recovered++;
            }
            if (p.mask) {
                stroke(0);
            }

            if (p.status == 3) deceased++;

            else circle(p.position.x, p.position.y, radius * 2);

        }

        //Draw rectangles
        stroke(100, 255, 100);
        fill(100, 255, 100);
        rect(0, boardHeight, boardWidth / 4, boardHeight + partitionHeight);

        stroke(255, 100, 100);
        fill(255, 100, 100);
        rect(boardWidth / 4, boardHeight, (boardWidth / 4) * 2, boardHeight + partitionHeight);

        stroke(100, 100, 255);
        fill(100, 100, 255);
        rect((boardWidth / 4) * 2, boardHeight, (boardWidth / 4) * 3, boardHeight + partitionHeight);

        stroke(100, 100, 100);
        fill(100, 100, 100);
        rect((boardWidth / 4) * 3, boardHeight, boardWidth, boardHeight + partitionHeight);

        //Draw text
        stroke(0);
        fill(0);
        line(0, boardHeight, boardWidth, boardHeight);
        textAlign(CENTER);
        if (infected == 0) {
            textSize(10);
            text("FIN", boardWidth / 2, boardHeight + partitionHeight - 5);
        }
        textSize((int) (width / 32));
        fill(0);
        text("Healthy: " + healthy, boardWidth / 8, boardHeight + ((partitionHeight / 3) * 2));
        text("Infected: " + infected, (boardWidth / 8) * 3, boardHeight + ((partitionHeight / 3) * 2));
        text("Recovered: " + recovered, (boardWidth / 8) * 5, boardHeight + ((partitionHeight / 3) * 2));
        text("Deceased: " + deceased, (boardWidth / 8) * 7, boardHeight + ((partitionHeight / 3) * 2));
    }

    public boolean randomChance(int chance) {
        int rand = (int) (1 + Math.random() * (100 - 1));
        if (rand <= chance) return true;
        else return false;
    }
}
