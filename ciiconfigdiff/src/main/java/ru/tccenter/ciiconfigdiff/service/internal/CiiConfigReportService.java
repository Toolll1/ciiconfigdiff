package ru.tccenter.ciiconfigdiff.service.internal;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class CiiConfigReportService {

    private static final List<String> ENV_COLUMNS = CiiConfigFetchService.ENVIRONMENTS;
    private static final String MISSING_VALUE = "—";

    /**
     * Формирует HTML-отчёт по результатам сравнения конфигураций.
     *
     * @param results результаты сравнения по группам конфигураций
     * @return HTML-строка с отчётом
     */
    String toHtml(List<CiiConfigDiffResult> results) {
        StringBuilder html = new StringBuilder();
        appendDocumentStart(html);
        appendBody(html, results);
        appendDocumentEnd(html);
        return html.toString();
    }

    /**
     * Добавляет начало HTML-документа, стили и заголовок отчёта.
     *
     * @param html буфер HTML-документа
     */
    private void appendDocumentStart(StringBuilder html) {
        html.append("<html>\n");
        html.append("  <head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        appendStyles(html);
        html.append("  </head>\n");
        html.append("  <body>\n");
        html.append("    <h1>CII Config Diff</h1>\n");
    }

    /**
     * Добавляет CSS-стили для таблиц отчёта.
     *
     * @param html буфер HTML-документа
     */
    private void appendStyles(StringBuilder html) {
        html.append("    <style>\n");
        html.append("      table { border-collapse: collapse; margin-bottom: 24px; width: 100%; }\n");
        html.append("      th, td { border: 1px solid #666; padding: 6px 8px; text-align: left; vertical-align: top; }\n");
        html.append("      th { background: #f2f2f2; }\n");
        html.append("    </style>\n");
    }

    /**
     * Добавляет тело отчёта: сообщение об отсутствии различий или таблицы по группам.
     *
     * @param html    буфер HTML-документа
     * @param results результаты сравнения
     */
    private void appendBody(StringBuilder html, List<CiiConfigDiffResult> results) {
        if (hasNoDifferences(results)) {
            html.append("    <p>Различий нет</p>\n");
            return;
        }
        results.stream()
                .filter(CiiConfigDiffResult::hasDifferences)
                .forEach(result -> appendGroupTable(html, result));
    }

    /**
     * Проверяет, отсутствуют ли различия во всех группах.
     *
     * @param results результаты сравнения
     * @return {@code true}, если различий нет
     */
    private boolean hasNoDifferences(List<CiiConfigDiffResult> results) {
        return results.stream().noneMatch(CiiConfigDiffResult::hasDifferences);
    }

    /**
     * Добавляет таблицу различий для одной группы конфигураций.
     *
     * @param html   буфер HTML-документа
     * @param result результат сравнения группы
     */
    private void appendGroupTable(StringBuilder html, CiiConfigDiffResult result) {
        appendGroupHeader(html, result);
        html.append("    <table>\n");
        appendTableHeader(html);
        appendTableRows(html, result.getFieldDiffs());
        html.append("    </table>\n");
    }

    /**
     * Добавляет заголовок группы конфигураций.
     *
     * @param html   буфер HTML-документа
     * @param result результат сравнения группы
     */
    private void appendGroupHeader(StringBuilder html, CiiConfigDiffResult result) {
        html.append("    <h2>")
                .append(escape(result.getCiiCode()))
                .append(" / ")
                .append(escape(result.getCiiGroup()))
                .append("</h2>\n");
    }

    /**
     * Добавляет строку заголовков таблицы с колонками контуров.
     *
     * @param html буфер HTML-документа
     */
    private void appendTableHeader(StringBuilder html) {
        html.append("      <tr>\n");
        html.append("        <th>Поле</th>\n");
        for (String env : ENV_COLUMNS) {
            html.append("        <th>").append(escape(env)).append("</th>\n");
        }
        html.append("      </tr>\n");
    }

    /**
     * Добавляет строки таблицы для всех различающихся полей.
     *
     * @param html       буфер HTML-документа
     * @param fieldDiffs список различий полей
     */
    private void appendTableRows(StringBuilder html, List<FieldDiff> fieldDiffs) {
        for (FieldDiff diff : fieldDiffs) {
            appendTableRow(html, diff);
        }
    }

    /**
     * Добавляет строку таблицы для одного различающегося поля.
     *
     * @param html буфер HTML-документа
     * @param diff различие поля
     */
    private void appendTableRow(StringBuilder html, FieldDiff diff) {
        html.append("      <tr>\n");
        html.append("        <td>").append(escape(diff.getFieldPath())).append("</td>\n");
        for (String env : ENV_COLUMNS) {
            appendValueCell(html, diff.getValuesByEnv().get(env));
        }
        html.append("      </tr>\n");
    }

    /**
     * Добавляет ячейку таблицы со значением поля для контура.
     *
     * @param html  буфер HTML-документа
     * @param value значение поля или {@code null}, если поле отсутствует
     */
    private void appendValueCell(StringBuilder html, String value) {
        String displayValue = value == null ? MISSING_VALUE : value;
        html.append("        <td>").append(escape(displayValue)).append("</td>\n");
    }

    /**
     * Добавляет закрывающие теги HTML-документа.
     *
     * @param html буфер HTML-документа
     */
    private void appendDocumentEnd(StringBuilder html) {
        html.append("  </body>\n");
        html.append("</html>\n");
    }

    /**
     * Экранирует специальные HTML-символы в строке.
     *
     * @param value исходная строка
     * @return безопасная для HTML строка
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
