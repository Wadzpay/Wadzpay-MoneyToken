import com.opencsv.bean.BeanField
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvBindByName
import com.opencsv.exceptions.CsvRequiredFieldEmptyException

/*This class is copied from StackOverflow https://stackoverflow.com/a/58833974.
* Its main purpose is to provide opportunity to use both CSVBindByName and CSVBindByPosition annotations*/

class CSVHeaderMappingHelper<T> : ColumnPositionMappingStrategy<T>() {
    @Throws(CsvRequiredFieldEmptyException::class)
    override fun generateHeader(bean: T): Array<String?> {
        val numColumns = fieldMap.values().size
        super.generateHeader(bean)
        val header = arrayOfNulls<String>(numColumns)
        var beanField: BeanField<*, *>?
        for (i in 0 until numColumns) {
            beanField = findField(i)
            val columnHeaderName = extractHeaderName(beanField)
            header[i] = columnHeaderName
        }
        return header
    }

    private fun extractHeaderName(beanField: BeanField<*, *>?): String {
        if (beanField == null || beanField.field == null || beanField.field.getDeclaredAnnotationsByType(
                CsvBindByName::class.java
            ).isEmpty()
        ) {
            return ""
        }
        val bindByNameAnnotation = beanField.field.getDeclaredAnnotationsByType(CsvBindByName::class.java)[0]
        return bindByNameAnnotation.column
    }
}
