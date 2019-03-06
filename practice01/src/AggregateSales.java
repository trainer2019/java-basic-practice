import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 売上ファイルから売上データを取得して合計金額を集計します。
 * コマンドライン引数で集計する商品名を指定することもできます。
 * 売上ファイルはプロジェクト内の \sales\201904.csv です。
 */

public class AggregateSales {
    private static final String ITEM_ALL = "全商品";
    private static final String SEPARATOR = ",";
    // ここに売上ファイルのパスをしていします。
    private static final String TARGET_PATH = "C:\\work\\Web基礎講座\\java-basic-practice\\practice01\\sales\\201904.csv";

    public static void main(String[] args) {

        // 集計する商品の指定があるか否かをコマンドライン引数から受け取ります。
        String targetItem = null;
        if (args.length > 0){
            targetItem = args[0];
        } else {
            targetItem = ITEM_ALL;
        }

        // 売上ファイルを読み込み、全データをList<String>型のsalesListに読み込みます。
        List<String> salesList = null;
        Path path = Paths.get(TARGET_PATH);
        try {
            salesList = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Files#readAllLinesは検査例外なので必ずtry～catch構文を使います。
            e.printStackTrace();
        }

        int totalSales = 0;
        for (String line : salesList) {

            // String#splitを使って読み込んだ1列分のデータを","で分割し、必要な項目を取り出します。
            String[] saleData = line.split(SEPARATOR);
            String itemName = saleData[1];
            int sale = Integer.parseInt(saleData[2]);

            // 全商品を集計する場合、又は指定した商品だった場合は合計金額に加算します。
            if ((targetItem.equals(ITEM_ALL) || targetItem.equals(itemName))) {
                totalSales += sale;
            }
        }

        // 集計した合計金額を出力します。
        // 売上がないときは、金額表示する必要はありません。
        if (totalSales > 0) {
            System.out.println("今月の " + targetItem + " の売り上げ合計は " + totalSales + " 円です。");
        } else {
            System.out.println("今月の " + targetItem + " の売り上げはありません。");
        }
    }
}
