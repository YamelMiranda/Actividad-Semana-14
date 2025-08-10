package example.actors;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

public class SupervisorActor extends AbstractBehavior<SupervisorActor.Command> {

    public interface Command {}

    public static class DelegateTask implements Command {
        public final int a, b;
        public final ActorRef<WorkerActor.Response> replyTo;

        public DelegateTask(int a, int b, ActorRef<WorkerActor.Response> replyTo) {
            this.a = a;
            this.b = b;
            this.replyTo = replyTo;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.supervise(
            Behaviors.setup(ctx -> new SupervisorActor(ctx))
        ).onFailure(RuntimeException.class, SupervisorStrategy.restart());
    }

    private final ActorRef<WorkerActor.Command> worker;

    private SupervisorActor(ActorContext<Command> ctx) {
        super(ctx);
        this.worker = ctx.spawn(WorkerActor.create(), "worker-actor");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(DelegateTask.class, this::onDelegateTask)
            .build();
    }

    private Behavior<Command> onDelegateTask(DelegateTask msg) {
        worker.tell(new WorkerActor.ProcessTask(msg.a, msg.b, msg.replyTo));
        return this;
    }
}
