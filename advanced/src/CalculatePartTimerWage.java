import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.List;

public class CalculatePartTimerWage {
    private static final String SEPARATOR = ",";
    private static final String PATH_PART_TIMERS = "アルバイト従業員一覧ファイル(PartTimers.csv)のパスを設定して下さい。";
    private static final String PATH_ATTENDANCES = "勤務履歴ファイル(Attendances.csv)のパスを設定して下さい。";
    private static final long ONE_HOUR = 1000 * 60 * 60;   // ミリ秒で1時間

    public static void main(String[] args) {

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

                // 勤務開始時間と退勤時間からミリ秒単位の労働時間を計算する
                Time startTime = Time.valueOf(attendanceDetail[2]);
                Time finishTime = Time.valueOf(attendanceDetail[3]);
                long workTimeByMillisecond = finishTime.getTime() - startTime.getTime();
                // ミリ秒単位から時間単位に直す
                BigDecimal workTimeByHour = BigDecimal.valueOf(workTimeByMillisecond / ONE_HOUR);

                /*
                 * 時給 × 労働時間で1日分の給与額を計算する
                 *
                 * 厳密には時給が1分単位で発生するのか？5分単位で発生するか？等を考慮して数値処理を行う必要があります。
                 * BigDecimalは厳密な数値処理を行うための機能が豊富に備わっています。
                 */
                BigDecimal dailyWage = partTimerWage.multiply(workTimeByHour).setScale(0, RoundingMode.UP);

                // 合計金額に加算
                totalWage = totalWage.add(dailyWage);
            }

            System.out.println(partTimerName + " さんのお給料は " + totalWage + " 円でした。");
        }
    }
}
