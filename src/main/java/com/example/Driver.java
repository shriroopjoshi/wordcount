package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;

public class Driver extends Configured implements Tool {

	private static Logger logger = Logger.getLogger(Driver.class);

	public static void main(String[] args) throws Exception {
		try {
			int res = ToolRunner.run(new Configuration(), (Tool) new Driver(), args);
			System.exit(res);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(255);
		}
	}

	public int run(String[] args) throws Exception {

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Your job name");

		job.setJarByClass(Driver.class);

		logger.info("job " + job.getJobName() + " [" + job.getJar() + "] started with the following arguments: "
				+ Arrays.toString(args));

		if (args.length < 2) {
			logger.warn("to run this jar are necessary at 2 parameters \"" + job.getJar()
					+ " input_files output_directory");
			return 1;
		}

		job.setMapperClass(WordcountMapper.class);
		logger.info("mapper class is " + job.getMapperClass());

		// job.setMapOutputKeyClass(Text.class);
		// job.setMapOutputValueClass(IntWritable.class);
		logger.info("mapper output key class is " + job.getMapOutputKeyClass());
		logger.info("mapper output value class is " + job.getMapOutputValueClass());

		job.setReducerClass(WordcountReducer.class);
		logger.info("reducer class is " + job.getReducerClass());
		job.setCombinerClass(WordcountReducer.class);
		logger.info("combiner class is " + job.getCombinerClass());
		// When you are not runnign any Reducer
		// OR job.setNumReduceTasks(0);
		// logger.info("number of reduce task is " + job.getNumReduceTasks());

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		logger.info("output key class is " + job.getOutputKeyClass());
		logger.info("output value class is " + job.getOutputValueClass());

		job.setInputFormatClass(TextInputFormat.class);
		logger.info("input format class is " + job.getInputFormatClass());

		job.setOutputFormatClass(TextOutputFormat.class);
		logger.info("output format class is " + job.getOutputFormatClass());

		Path filePath = new Path(args[0]);
		logger.info("input path " + filePath);
		FileInputFormat.setInputPaths(job, filePath);

		Path outputPath = new Path(args[1]);
		logger.info("output path " + outputPath);
		FileOutputFormat.setOutputPath(job, outputPath);

		job.waitForCompletion(true);

		final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
		final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load("/home/admin1/twitter/tweet-file");
		final Dimension dimension = new Dimension(600, 600);
		final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(2);
		wordCloud.setBackground(new CircleBackground(300));
		wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1),
				new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
		wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
		wordCloud.build(wordFrequencies);
		wordCloud.writeToFile("/home/admin1/twitter/output.png");

		return 0;
	}
}
