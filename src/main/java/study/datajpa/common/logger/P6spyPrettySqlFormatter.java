package study.datajpa.common.logger;

import static org.springframework.util.StringUtils.hasText;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import org.hibernate.engine.jdbc.internal.FormatStyle;

public class P6spyPrettySqlFormatter implements MessageFormattingStrategy {

    private static boolean isDdl(String trimmedSQL) {
        return trimmedSQL.startsWith("create") || trimmedSQL.startsWith("alter")
            || trimmedSQL.startsWith("comment");
    }

    private static String trim(String sql) {
        return sql.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isStatement(String category) {
        return Category.STATEMENT.getName().equals(category);
    }

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
        String prepared, String sql, String url) {
        String formattedSql = formatSql(category, sql);
        return formatLog(elapsed, category, formattedSql);
    }

    private String formatSql(String category, String sql) {
        if (hasText(sql) && isStatement(category)) {
            String trimmedSQL = trim(sql);
            if (isDdl(trimmedSQL)) {
                return FormatStyle.DDL.getFormatter().format(sql);
            }
            return FormatStyle.BASIC.getFormatter().format(sql); // maybe DML
        }
        return sql;
    }

    private String formatLog(long elapsed, String category, String formattedSql) {
        return String.format("[%s] | %d ms | %s", category, elapsed,
            formatSql(category, formattedSql));
    }
}
