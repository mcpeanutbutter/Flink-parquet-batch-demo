package demo

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.core.fs.Path
import org.apache.flink.formats.parquet.avro.AvroParquetWriters
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.Expressions.col
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment
import org.apache.flink.types.Row

object Job {

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {

        val fileParams: ParameterTool = ParameterTool.fromArgs(args)
        val inputPath = fileParams.getRequired("input_path")
        val outputPath = fileParams.getRequired("output_path")

        val env = StreamExecutionEnvironment.getExecutionEnvironment()

        val tableEnv = StreamTableEnvironment.create(env)

        val query = """
            CREATE TABLE data (letter STRING, timedigit BIGINT) WITH (
                'connector' = 'filesystem',
                'path' = '${inputPath}',
                'format' = 'parquet'
            )
            """

        tableEnv.executeSql(query)

        val dataTable = tableEnv.from("data")

        val parquetSink = FileSink
            .forBulkFormat(Path(outputPath), AvroParquetWriters.forReflectRecord(Data::class.java))
            .build()

        tableEnv
            .toDataStream(dataTable)
            .assignTimestampsAndWatermarks(
                WatermarkStrategy.noWatermarks<Row>().withTimestampAssigner { row, _ -> row.getFieldAs("timedigit") }
            )
            .map { row -> Data(row.getFieldAs("timedigit"), row.getFieldAs("letter")) }
            .sinkTo(parquetSink)

        env.execute("Read from and write to parquet")
    }
}
