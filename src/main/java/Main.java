import analyzer.AnalyzerApp;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Main {
    public static void main(String[] args) {
        AnalyzerApp analyzerApp = new AnalyzerApp(System.out);
        analyzerApp.getStatistics(args);
    }
}
