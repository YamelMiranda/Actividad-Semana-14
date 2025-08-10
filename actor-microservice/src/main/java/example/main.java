package example;

import akka.actor.typed.ActorSystem;
import example.actors.*;

public class Main {
    public static void main(String[] args) {
        var system = ActorSystem.create(SupervisorActor.create(), "system");
        
    }
}
