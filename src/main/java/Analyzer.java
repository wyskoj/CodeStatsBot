import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class Analyzer {
	final static Pattern javaRegex = Pattern.compile("\\s*([*/]{2})|(\\* )");
	
	static class Analysis {
		final int totalLines;
		final int codeLines;
		final int commentLines;
		final int emptyLines;
		final String fileName;
		
		public Analysis(int totalLines, int codeLines, int commentLines, int emptyLines, String fileName) {
			this.totalLines = totalLines;
			this.codeLines = codeLines;
			this.commentLines = commentLines;
			this.emptyLines = emptyLines;
			this.fileName = fileName;
		}
		
		@Override
		public String toString() {
			return "Analysis{" +
					"totalLines=" + totalLines +
					", codeLines=" + codeLines +
					", commentLines=" + commentLines +
					", emptyLines=" + emptyLines +
					'}';
		}
	}
	
	/**
	 * Renders a chart displaying the information in the specified analysis.
	 *
	 * @param analysis the analysis to graph
	 * @return a file pointing to the graphic
	 */
	public static File buildChart(Analysis analysis) throws IOException {
		String title = "Code analysis of " + analysis.fileName;
		PieChart pieChart =
				new PieChartBuilder().width(800).height(600).title(title).build();
		pieChart.getStyler().setChartTitleVisible(true).setLegendPosition(Styler.LegendPosition.OutsideE);
		pieChart.addSeries("Comments", analysis.commentLines);
		pieChart.addSeries("Code", analysis.codeLines);
		pieChart.addSeries("Linebreaks", analysis.emptyLines);
		
		CategoryChart barChart = new CategoryChartBuilder().width(800).height(600).title(title).build();
		barChart.addSeries("Data", Arrays.asList("Comments", "Code", "Linebreaks"), Arrays.asList(analysis.commentLines,
				analysis.codeLines, analysis.emptyLines));
		barChart.getStyler().setLegendVisible(false);
		
		
		BitmapEncoder.saveBitmap(Arrays.asList(pieChart, barChart), 1, 2, "analysis.png", BitmapEncoder.BitmapFormat.PNG);
		return new File("analysis.png");
	}
	
	@Contract("_ -> new")
	public static @NotNull Analysis analyzeCodeFile(@NotNull File file) throws IOException {
		Scanner scanner;
		
		if (file.getName().endsWith(".zip")) {
			ZipFile zipFile = new ZipFile(file);
			new File("analyze/uncompressed").mkdir();
		}
		
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			return new Analysis(0, 0, 0, 0, null);
		}
		int totalLines = 0;
		int codeLines = 0;
		int commentLines = 0;
		int emptyLines = 0;
		while (scanner.hasNextLine()) {
			totalLines++;
			String line = scanner.nextLine();
			if (line.isBlank()) {
				emptyLines++;
			} else if (javaRegex.matcher(line).find()) {
				commentLines++;
			} else {
				codeLines++;
			}
		}
		
		return new Analysis(totalLines, codeLines, commentLines, emptyLines, file.getName());
	}
}
