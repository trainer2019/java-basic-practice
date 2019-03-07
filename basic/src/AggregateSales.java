import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 売上ファイルから売上データを取得して合計金額を集計します。
 * ポイントは、ファイルから1列ずつデータを読み込んでString#splitで分割して処理しやすくすることです。
 */
public class AggregateSales {
    private static final String ITEM_ALL = "全商品";
    private static final String SEPARATOR = ",";
    private static final String PATH_SALES = "ローカル環境に合わせて売上ファイル(sales.csv)のパスを設定して下さい。";

    public static void main(String[] args) {

        // 集計する商品の指定があるか否かをコマンドライン引数から受け取ります。
        // 存在しない商品かチェックできる処理を実装できるとより素晴らしいですね。
        String targetItem = null;
        if (args.length > 0) {
            targetItem = args[0];
        } else {
            targetItem = ITEM_ALL;
        }

        // 売上ファイルを読み込み、全データをList<String>型のsalesListに読み込みます。
        List<String> sales = null;
        Path path = Paths.get(PATH_SALES);
        try {
            sales = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Files#readAllLinesは検査例外なので必ずtry～catch構文を使います。
            System.out.println("[ERROR] 売上ファイルの読み込みに失敗しました。");
            e.printStackTrace();
        }

        int totalSales = 0;
        for (String sale : sales) {

            // String#splitを使って読み込んだ1列分のデータを","で分割し、必要な項目を取り出します。
            String[] saleData = sale.split(SEPARATOR);
            String itemName = saleData[1];
            int itemSale = Integer.parseInt(saleData[2]);

            // 全商品を集計する場合、又は指定した商品だった場合は合計金額に加算します。
            if ((targetItem.equals(ITEM_ALL) || targetItem.equals(itemName))) {
                totalSales += itemSale;
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
