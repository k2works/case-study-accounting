package com.example.accounting.documentation;

import com.example.accounting.TestcontainersConfiguration;
import jig.erd.JigErd;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.nio.file.Path;

/**
 * JIG-ERD を使用した ER 図生成テスト
 *
 * <p>このテストを実行すると、データベースから ER 図を生成します。
 * 生成された図は build/jig-erd ディレクトリに出力されます。</p>
 *
 * <p>Graphviz がインストールされていない場合は DOT ファイルのみ出力されます。</p>
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("JIG-ERD ER図生成")
class JigErdGeneratorTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("ER図を生成する")
    void generateErDiagram() {
        Path outputDir = Path.of("build/jig-erd");
        outputDir.toFile().mkdirs();

        // JIG-ERD でER図を生成
        // 出力先は jig-erd.properties または環境変数で設定可能
        JigErd.run(dataSource);

        System.out.println("ER図を生成しました: " + outputDir.toAbsolutePath());
    }
}
