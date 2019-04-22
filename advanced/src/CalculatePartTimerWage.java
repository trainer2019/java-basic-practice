import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.List;

/**
 * アルバイト従業員一覧と勤怠履歴を照合してお給料を計算します。
 * ポイントは次の3点あります。
 * ・ダブルループという、繰り返し処理を二重に行う手法を使ってファイルを照合します。
 * ・給与計算のような複雑な数値処理を行う場合はBigDecimalを使うほうが良いです。
 * ・DateやTimeのような日時に関するデータはlong型で扱うと計算が行いやすくなります。
 */
public class CalculatePartTimerWage {
	public static void main(String[] args) {

		// 定数を宣言
		final String SEPARATOR = ",";
		final String PATH_PART_TIMERS = "アルバイト従業員一覧ファイル(PartTimers.csv)のパスを設定して下さい。";
		final String PATH_ATTENDANCES = "勤務履歴ファイル(Attendances.csv)のパスを設定して下さい。";
		final long ONE_MINUTE = 1000 * 60; // ミリ秒で1分

		// アルバイト従業員一覧を読み込み、partTimersリストに格納します。
		List<String> partTimers = null;
		Path pathPartTimers = Paths.get(PATH_PART_TIMERS);
		try {
			partTimers = Files.readAllLines(pathPartTimers, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// Files#readAllLinesは検査例外なので必ずtry～catch構文を使います。
			System.out.println("[ERROR] アルバイト従業員一覧ファイルの読み込みに失敗しました。");
			e.printStackTrace();
		}

		// 勤怠履歴を読み込み、attendancesリストに格納します。
		List<String> attendances = null;
		Path pathAttendances = Paths.get(PATH_ATTENDANCES);
		try {
			attendances = Files.readAllLines(pathAttendances, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("[ERROR] 勤怠履歴ファイルの読み込みに失敗しました。");
			e.printStackTrace();
		}

		// ダブルループで従業員ごとのアルバイト代を計算する
		for (String partTimer : partTimers) {
			String[] partTimerDetail = partTimer.split(SEPARATOR);

			// アルバイト従業員のデータを取得
			String partTimerId = partTimerDetail[0];
			String partTimerName = partTimerDetail[1];
			// 今回はBigDecimalを使ってみます。
			BigDecimal partTimerWage = new BigDecimal(partTimerDetail[2]);

			BigDecimal totalWage = BigDecimal.ZERO;

			for (String attendance : attendances) {
				String[] attendanceDetail = attendance.split(SEPARATOR);

				// 計算中のアルバイト従業員の勤怠履歴か判定
				String partTimerIdOfAttendanceRecode = attendanceDetail[1];
				if (!partTimerId.equals(partTimerIdOfAttendanceRecode)) {
					// 違った場合は次の繰り返し処理に移る
					continue;
				}

				// 勤務開始時間と退勤時間から分単位の労働時間を計算する
				Time startTime = Time.valueOf(attendanceDetail[2]);
				Time finishTime = Time.valueOf(attendanceDetail[3]);
				long workTimeByMillisecond = finishTime.getTime() - startTime.getTime();
				long workTimeByMinute = workTimeByMillisecond / ONE_MINUTE;

				// 勤務時間を時間と分の両方で算出する
				BigDecimal workHour = new BigDecimal(workTimeByMinute / 60);
				BigDecimal workMinute = new BigDecimal(workTimeByMinute % 60).divide(new BigDecimal(60));

				// 時給 × 労働時間で1日分の給与額を計算する
				BigDecimal dailyWage = workHour.multiply(partTimerWage).add(workMinute.multiply(partTimerWage))
						.setScale(0, RoundingMode.DOWN);

				// 合計金額に加算
				totalWage = totalWage.add(dailyWage);
			}

			System.out.println(partTimerName + " さんのお給料は " + totalWage + " 円でした。");
		}
	}
}
