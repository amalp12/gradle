import com.h0tk3y.kotlin.staticObjectNotation.*
import com.h0tk3y.kotlin.staticObjectNotation.ElementOrFailureResult.ElementResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BasicDataTest {
    @Test
    fun `parses literals`() {
        val results = parse(
            """
            a = 1
            b = "test"
            c = ${'"'}""test${'"'}""
            e = true
            d = false
            """.trimIndent()
        )

        assertAll(results.map {
            {
                assertTrue(it is ElementResult)
                val element = it.element
                assertTrue(element is Assignment)
                assertTrue(element.rhs is Literal<*>)
            }
        })
        val values = results.map { (((it as ElementResult).element as Assignment).rhs as Literal<*>).value }
        assertEquals(listOf(1, "test", "test", true, false), values)
    }
    
    @Test
    fun `parses imports`() {
        val results = parse(
            """
                import a.b.c
                import a.b.MyData
                import MyOtherData
            """.trimIndent()
        )
        
        val expected = listOf("a.b.c", "a.b.MyData", "MyOtherData")
        assertAll(results.mapIndexed { index, it ->
            {
                assertIs<ElementResult<*>>(it)
                val element = it.element
                assertIs<Import>(element)
                assertEquals(expected[index], element.name.nameParts.joinToString("."))
            }
        })
    }

    @Test
    fun `parses function invocations without access chains`() {
        val results = parse(
            """
            f(x = y)
            f(1)
            """.trimIndent()
        )

        assertAll(results.map {
            {
                assertTrue(it is ElementResult)
                assertTrue(it.element is FunctionCall)
            }
        })
    }
    
    @Test
    fun `parses function invocation after an access chain`() {
        val result = parse("""
            f.g.h.i.j.k(test)
        """.trimIndent()).single()
        
        assertTrue(result is ElementResult && result.element is FunctionCall)
    }

    @Test
    fun `parses positional parameters`() {
        val result = parse(
            """
            f(1, x, g())
            """.trimIndent()
        ).single()
        assertIs<ElementResult<*>>(result)
        val element = result.element
        assertIs<FunctionCall>(element)
        assertAll(element.args.map {
            { assertIs<FunctionArgument.Positional>(it) }
        })
    }

    @Test
    fun `parses named arguments`() {
        val result = parse(
            """
            f(a = b, c = d)            
            """.trimIndent()
        ).single()
        assertIs<ElementResult<*>>(result)
        val element = result.element
        assertIs<FunctionCall>(element)
        assertTrue(element.args.size == 2)
        val arg0 = element.args[0]
        assertIs<FunctionArgument.Named>(arg0)
        assertEquals("a", arg0.name)
        val arg1 = element.args[1]
        assertIs<FunctionArgument.Named>(arg1)
        assertEquals("c", arg1.name)
    }

    @Test
    fun `parses an assignment chain`() {
        val result = parse("a.b.c = 1").single() as ElementResult
        val element = result.element
        assertIs<Assignment>(element)
        assertEquals(listOf("a", "b", "c"), element.lhs.nameParts)
    }
    
    @Test
    fun `parses a local val`() {
        val result = parse("val a = 1").single()
        assertIs<ElementResult<*>>(result)
        val element = result.element
        assertIs<LocalValue>(element)
        assertEquals("a", element.name)
        assertIs<Literal.IntLiteral>(element.rhs)
    }

    @Test
    fun `parses access chain in rhs`() {
        val result = parse("a = b.c.d").single()
        assertIs<ElementResult<*>>(result)
        val element = result.element
        assertIs<Assignment>(element)
        assertIs<AccessChain>(element.rhs)
    }

    @Test
    fun `parses lambdas`() {
        val result = parse("a { b = 1 }").single() as ElementResult
        val element = result.element
        assertIs<FunctionCall>(element)
        val arg = element.args.single()
        assertIs<FunctionArgument.Lambda>(arg)
        assertIs<Assignment>(arg.block.statements.single())
    }
}