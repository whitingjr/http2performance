package eu.janinko.foobar.httpmetrics;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 *
 * @author jbrazdil
 */
public class Main {

    private static final int THREAD_POOL_SIZE = 25; // How much threads are in the task pool
    private static final int LIMIT = 100; // How much lines from the input to read

    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws InterruptedException, IOException {
        Main main = new Main();
        int repeates = 1;
        if (args.length > 0) {
            repeates = Integer.parseInt(args[0]);
        }

        List<String> urls = Files.lines(Paths.get("centralUrls.txt")).limit(LIMIT).collect(Collectors.toList());

        List<TaskProvider> providers = new ArrayList<>();
        providers.add(new JavaHttpClientTaskprovider(urls, HttpClient.Version.HTTP_1_1));
        providers.add(new JavaHttpClientTaskprovider(urls, HttpClient.Version.HTTP_2));
        providers.add(new Appache11TaskProvider(urls));
        providers.add(new Appache2TaskProvider(urls));

        Map<TaskProvider, List<Double>> averages = new HashMap<>();
        for (int i = 0; i < repeates; i++) {
            Map<TaskProvider, Double> results = main.runAllTasks(providers);
            for (Map.Entry<TaskProvider, Double> e : results.entrySet()) {
                TaskProvider key = e.getKey();
                Double value = e.getValue();
                List<Double> list = averages.computeIfAbsent(key, k -> new ArrayList<>());
                list.add(value);
            }
        }
        System.out.println();
        for (TaskProvider provider : providers) {
            System.out.print(provider.getName() + ": ");
            printStats(averages.get(provider));
            System.out.println();
        }

        main.close();
    }

    private void close() {
        executorService.shutdown();
    }

    private double average(List<Long> results) {
        double sum = 0;
        for (double result : results) {
            sum += result;
        }
        int count = results.size();
        double avg = sum / count;
        return avg / 1000000.0;
    }

    private static void printStats(List<Double> results) {
        double sum = 0;
        double sumsq = 0;
        for (double result : results) {
            sum += result;
            sumsq += result * result;
        }
        int count = results.size();
        double avg = sum / count;
        double std = Math.sqrt((sumsq / count) - avg * avg);
        System.out.printf("%.2f / %d = %.3f ± %.3f ms", sum, count, avg, std);
    }

    public Map<TaskProvider, Double> runAllTasks(Collection<TaskProvider> providers) throws InterruptedException {
        Map<TaskProvider, Double> results = new HashMap<>();
        for (TaskProvider provider : providers) {
            results.put(provider, runTasks(provider));
        }
        return results;
    }

    public double runTasks(TaskProvider provider) throws InterruptedException {
        List<Future<Long>> htttp1results = executorService.invokeAll(provider.getTasks());
        final List<Long> results = await(htttp1results);
        final double result = average(results);

        System.out.printf("%s measurements: %.3f ms %n", provider.getName(), result);
        return result;
    }

    public <T> List<T> await(List<Future<T>> futures) {
        return futures.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
    }
}
