package com.vacuumlabs.wadzpay.utils
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import kotlin.jvm.Throws

class PostgreSQLIntegerArrayType : UserType {

    override fun sqlTypes(): IntArray {
        return intArrayOf(Types.ARRAY)
    }

    override fun returnedClass(): Class<*> {
        return Array<Int>::class.java
    }

    @Throws(HibernateException::class, SQLException::class)
    override fun nullSafeGet(
        rs: ResultSet,
        names: Array<out String>,
        session: SharedSessionContractImplementor,
        owner: Any
    ): Any? {
        val array = rs.getArray(names[0])
        return array?.array as Array<Int>?
    }

    @Throws(HibernateException::class, SQLException::class)
    override fun nullSafeSet(
        st: PreparedStatement,
        value: Any?,
        index: Int,
        session: SharedSessionContractImplementor
    ) {
        val connection = st.connection
        if (value == null) {
            st.setNull(index, Types.ARRAY)
        } else {
            val array = connection.createArrayOf("integer", value as Array<*>)
            st.setArray(index, array)
        }
    }

    override fun equals(x: Any?, y: Any?): Boolean {
        return x === y || x != null && y != null && x == y
    }

    override fun hashCode(x: Any?): Int {
        return x?.hashCode() ?: 0
    }

    override fun deepCopy(value: Any?): Any? {
        return value?.let { (it as Array<*>).copyOf() }
    }

    override fun isMutable(): Boolean {
        return true
    }

    override fun assemble(cached: java.io.Serializable, owner: Any): Any? {
        return deepCopy(cached)
    }

    override fun disassemble(value: Any): java.io.Serializable {
        return value as Serializable
    }

    override fun replace(original: Any?, target: Any?, owner: Any): Any? {
        return deepCopy(original)
    }
}
