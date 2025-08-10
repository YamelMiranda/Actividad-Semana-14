package example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import example.actors.*;
import akka.actor.typed.*;
import akka.actor.typed.javadsl.AskPattern;
import akka.util.Timeout;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

public class Handler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final ActorSystem<SupervisorActor.Command> actorSystem =
        ActorSystem.create(SupervisorActor.create(), "actorSystem");

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        int a = (int) input.getOrDefault("a", 0);
        int b = (int) input.getOrDefault("b", 0);

        Timeout timeout = Timeout.create(Duration.ofSeconds(3));
        CompletionStage<WorkerActor.Response> response =
            AskPattern.ask(actorSystem,
                replyTo -> new SupervisorActor.DelegateTask(a, b, replyTo),
                timeout,
                actorSystem.scheduler());

        try {
            int result = response.toCompletableFuture().get();
            return Map.of("result", result);
        } catch (Exception e) {
            return Map.of("error", "Failed: " + e.getMessage());
        }
    }
}
