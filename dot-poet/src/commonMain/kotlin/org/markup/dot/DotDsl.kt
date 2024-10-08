package org.markup.dot

import java.io.File

// Define the data classes with rendering functions
data class Graph(
    val rankdir: String? = null,
    val name: String? = null,
    val directed: Boolean = false,
    val nodes: MutableList<Node> = mutableListOf(),
    val edges: MutableList<Edge> = mutableListOf()
) {
    fun render(): String {
        val graphType = if (directed) "digraph" else "graph"
        val rankdirAttr = rankdir?.let { "rankdir=\"$it\"; \n" } ?: ""
        val namePart = name?.let { "$graphType $it" } ?: graphType

        val nodesPart = nodes.joinToString("\n") { it.render() }
        val edgesPart = edges.joinToString("\n") { it.render(directed) }
        return "$namePart {\n$rankdirAttr\n$nodesPart\n$edgesPart\n}"
    }

    fun toFile(name: String) {
        File(name).writeText(render())
    }
}

data class Node(val id: String, val style: NodeStyle? = null) {
    fun render(): String {
        val stylePart = style?.let { "[${it.render()}]" } ?: ""
        return "$id $stylePart;"
    }
}

data class Edge(val from: Node, val to: Node, val style: EdgeStyle? = null) {
    fun render(directed: Boolean): String {
        val edgeOp = if (directed) "->" else "--"
        val stylePart = style?.let { "[${it.render()}]" } ?: ""
        return "${from.id} $edgeOp ${to.id} $stylePart;"
    }
}

data class NodeStyle(val shape: String? = null, val color: String? = null, val label: String? = null) {
    fun render(): String {
        val attributes = mutableListOf<String>()
        shape?.let { attributes.add("shape=\"$it\"") }
        color?.let { attributes.add("color=\"$it\"") }
        label?.let { attributes.add("label=\"$it\"") }
        return attributes.joinToString(", ")
    }
}

data class EdgeStyle(val style: String? = null, val color: String? = null, val label: String? = null) {
    fun render(): String {
        val attributes = mutableListOf<String>()
        style?.let { attributes.add("style=\"$it\"") }
        color?.let { attributes.add("color=\"$it\"") }
        label?.let { attributes.add("label=\"$it\"") }
        return attributes.joinToString(", ")
    }
}

// Builder classes to construct the domain model
class GraphBuilder {
    private var name: String? = null
    private var directed: Boolean = false
    private val nodes = mutableListOf<Node>()
    private val edges = mutableListOf<Edge>()

    private var rankdir: String? = null


    fun name(name: String) {
        this.name = name
    }

    fun rankdir(rankdir: String) {
        this.rankdir = rankdir
    }

    fun directed() {
        this.directed = true
    }

    fun undirected() {
        this.directed = false
    }

    fun node(id: String, init: NodeBuilder.() -> Unit = {}) {
        val node = NodeBuilder(id).apply(init).build()
        nodes.add(node)
    }

    fun edge(from: String, to: String, init: EdgeBuilder.() -> Unit = {}) {
        val fromNode = nodes.find { it.id == from } ?: throw IllegalArgumentException("Node $from not found")
        val toNode = nodes.find { it.id == to } ?: throw IllegalArgumentException("Node $to not found")
        val edge = EdgeBuilder(fromNode, toNode).apply(init).build()
        edges.add(edge)
    }

    fun build(): Graph {
        return Graph(rankdir = rankdir, name = name, directed = directed, nodes = nodes, edges = edges)
    }
}

class NodeBuilder(private val id: String) {
    private var shape: String? = null
    private var color: String? = null
    private var label: String? = null

    fun shape(shape: String) {
        this.shape = shape
    }

    fun label(label: String) {
        this.label = label
    }

    fun color(color: String) {
        this.color = color
    }

    fun build(): Node {
        return Node(id, NodeStyle(shape, color, label))
    }
}

class EdgeBuilder(private val from: Node, private val to: Node) {
    private var style: String? = null
    private var color: String? = null
    private var label: String? = null


    fun style(style: String) {
        this.style = style
    }

    fun label(label: String) {
        this.label = label
    }


    fun color(color: String) {
        this.color = color
    }

    fun build(): Edge {
        return Edge(from, to, EdgeStyle(style, color, label))
    }
}

// Extension functions for more readable DSL
fun graph(init: GraphBuilder.() -> Unit): Graph {
    return GraphBuilder().apply(init).build()
}