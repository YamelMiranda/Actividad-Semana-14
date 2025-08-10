package example.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class WorkerActor extends AbstractBehavior<WorkerActor.Command> {

    public interface Command {}

    public static class ProcessTask implements Command {
        public final int a, b;
        public final ActorRef<Response> replyTo;

        public ProcessTask(int a, int b, ActorRef<Response> replyTo) {
            this.a = a;
            this.b = b;
            this.replyTo = replyTo;
        }
    }

    public static class Response {
        public final int result;

        public Response(int result) {
            this.result = result;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerActor::new);
    }

    private WorkerActor(ActorContext<Command> ctx) {
        super(ctx);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(ProcessTask.class, this::onProcessTask)
            .build();
    }

    private Behavior<Command> onProcessTask(ProcessTask msg) {
        if (msg.a == -1) throw new RuntimeException("Simulated error");
        int sum = msg.a + msg.b;
        msg.replyTo.tell(new Response(sum));
        return this;
    }
}
