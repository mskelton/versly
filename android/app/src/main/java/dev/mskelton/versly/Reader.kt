package dev.mskelton.versly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mskelton.versly.persistence.Passage
import org.json.JSONArray

@Composable
fun Reader(passage: Passage) {
    val nodes = JSONArray(passage.data)
    val chapterId = passage.id.split(".")[1]

    Column {
        Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = passage.bookTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = chapterId,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        for (i in 0 until nodes.length()) {
            val node = nodes.getJSONArray(i)

            when (node.getString(0)) {
                "s1", "s2", "s3", "ms" -> Text(
                    text = node.getString(1),
                    modifier = Modifier.padding(0.dp, 32.dp, 0.dp, 16.dp),
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                )

                "b" -> Box(modifier = Modifier.height(16.dp))

//                "table" ->

                // iex: 'text-zinc-600 dark:text-zinc-400',
                "iex" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(fontStyle = FontStyle.Italic),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                )

                "d" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                    style = TextStyle(fontStyle = FontStyle.Italic),
                )

                "sp" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 0.dp),
                    style = TextStyle(fontStyle = FontStyle.Italic),
                )

                "pm" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent(8.sp)),
                )

                "pmo", "pmc" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 8.dp),
                )

                "pmr" -> ReaderChildNode(
                    node,
                    modifier = Modifier
                        .padding(16.dp, 0.dp, 0.dp, 8.dp)
                        .align(Alignment.End),
                )

                "pc" -> ReaderChildNode(
                    node,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 8.dp)
                        .align(Alignment.CenterHorizontally),
                )

                "pr", "cls" -> ReaderChildNode(
                    node,
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 0.dp, 8.dp)
                        .align(Alignment.End),
                )

                "pi1" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent(16.sp)),
                )

                "pi2" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(32.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent(16.sp)),
                )

                "pi3" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(48.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent(16.sp)),
                )

                "q1" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent((-16).sp)),
                )

                "q2" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(32.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent((-16).sp)),
                )

                "q3" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(48.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent((-16).sp)),
                )

                "q4" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(64.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent((-16).sp)),
                )

                "qa" -> ReaderChildNode(
                    node,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = TextStyle(fontStyle = FontStyle.Italic),
                )

                "qr" -> ReaderChildNode(
                    node,
                    modifier = Modifier.align(Alignment.End),
                )

                "qc" -> ReaderChildNode(
                    node,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                "qm1" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(32.dp, 0.dp, 0.dp, 0.dp),
                    style = TextStyle(textIndent = TextIndent((-16).sp)),
                )

                "qm2" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(48.dp, 0.dp, 0.dp, 0.dp),
                    style = TextStyle(textIndent = TextIndent((-16).sp)),
                )

                "li1", "lim" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
                )

                "li2" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(32.dp, 0.dp, 0.dp, 0.dp),
                )

                "li3" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(48.dp, 0.dp, 0.dp, 0.dp),
                )

                "li4" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(64.dp, 0.dp, 0.dp, 0.dp),
                )

                "m" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp),
                )

                "mi" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 8.dp),
                )

                "p" -> ReaderChildNode(
                    node,
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp),
                    style = TextStyle(textIndent = TextIndent(16.sp)),
                )

                else -> ReaderChildNode(node)
            }
        }
    }
}

@Composable
fun ReaderChildNode(
    node: JSONArray,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 18.sp,
    lineHeight: TextUnit = 36.sp,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    style: TextStyle = TextStyle(),
) {
    val children = node.getJSONArray(1)

    Text(
        text = buildAnnotatedString {
            for (j in 0 until children.length()) {
                val childNode = children.getJSONArray(j)

                when (val type = childNode.getString(0)) {
                    "v" -> withStyle(
                        style = SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        ),
                    ) {
                        append(childNode.getString(1) + " ")
                    }

                    // TODO: Float right
                    "qs" -> withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(childNode.getString(1))
                    }

                    // TODO: Float right
                    "litl" -> {
                        append(childNode.getString(1))
                    }

                    "wj" -> withStyle(style = SpanStyle(color = Color.Red)) {
                        append(childNode.getString(1))
                    }

                    "em", "bd" -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(childNode.getString(1))
                    }

                    "bk", "qt", "sig", "sls", "tl", "it" -> withStyle(
                        SpanStyle(fontStyle = FontStyle.Italic)
                    ) {
                        append(childNode.getString(1))
                    }

                    "nd", "sc" -> withStyle(SpanStyle(fontFeatureSettings = "smcp")) {
                        append(childNode.getString(1))
                    }

                    "sup" -> withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = 12.sp,
                        )
                    ) {
                        append(childNode.getString(1))
                    }

                    "t" -> append(childNode.getString(1))

                    else -> error("Unknown node type: $type")
                }
            }
        },
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight,
        style = style,
    )
}

@Preview(showBackground = true)
@Composable
fun ReaderPreview() {
    Reader(
        passage = Passage(
            id = "JHN.3.${DEFAULT_TRANSLATION}",
            bookTitle = "John",
            data = "[[\"s1\",\"You Must Be Born Again\"],[\"p\",[[\"t\",\"  \"],[\"v\",\"1\"],[\"t\",\"Now there was a man of the Pharisees named \"],[\"t\",\"Nicodemus, \"],[\"t\",\"a ruler of the Jews. \"],[\"v\",\"2\"],[\"t\",\"This man came to Jesus\"],[\"t\",\"  \"],[\"t\",\"by night and said to him, \"],[\"t\",\"“Rabbi, \"],[\"t\",\"we know that you are a teacher come from God, for no one can do these signs that you do \"],[\"t\",\"unless God is with him.” \"],[\"v\",\"3\"],[\"t\",\"Jesus answered him, \"],[\"wj\",\"“Truly, truly, I say to you, unless one is \"],[\"wj\",\"born \"],[\"wj\",\"again\"],[\"t\",\"  \"],[\"wj\",\"he cannot \"],[\"wj\",\"see the kingdom of God.”\"],[\"t\",\"  \"],[\"v\",\"4\"],[\"t\",\"Nicodemus said to him, “How can a man be born when he is old? Can he enter a second time into his mother’s womb and be born?” \"],[\"v\",\"5\"],[\"t\",\"Jesus answered, \"],[\"wj\",\"“Truly, truly, I say to you, unless one is born \"],[\"wj\",\"of water and the Spirit, he cannot enter the kingdom of God.\"],[\"t\",\"  \"],[\"v\",\"6\"],[\"wj\",\"That which is born of the flesh is \"],[\"wj\",\"flesh, and that which is born of the Spirit is spirit.\"],[\"t\",\"  \"],[\"v\",\"7\"],[\"wj\",\"Do not marvel that I said to you, ‘You\"],[\"t\",\"  \"],[\"wj\",\"must be born \"],[\"wj\",\"again.’\"],[\"t\",\"  \"],[\"v\",\"8\"],[\"wj\",\"The wind\"],[\"t\",\"  \"],[\"wj\",\"blows \"],[\"wj\",\"where it wishes, and you hear its sound, but you do not know where it comes from or where it goes. So it is with everyone who is born of the Spirit.”\"]]],[\"p\",[[\"t\",\"  \"],[\"v\",\"9\"],[\"t\",\"Nicodemus said to him, \"],[\"t\",\"“How can these things be?” \"],[\"v\",\"10\"],[\"t\",\"Jesus answered him, \"],[\"wj\",\"“Are you the teacher of Israel \"],[\"wj\",\"and yet you do not understand these things?\"],[\"t\",\"  \"],[\"v\",\"11\"],[\"wj\",\"Truly, truly, I say to you, \"],[\"wj\",\"we speak of what we know, and bear witness to what we have seen, but \"],[\"wj\",\"you\"],[\"t\",\"  \"],[\"wj\",\"do not receive our testimony.\"],[\"t\",\"  \"],[\"v\",\"12\"],[\"wj\",\"If I have told you earthly things and you do not believe, how can you believe if I tell you heavenly things?\"],[\"t\",\"  \"],[\"v\",\"13\"],[\"wj\",\"No one has \"],[\"wj\",\"ascended into heaven except \"],[\"wj\",\"he who descended from heaven, the Son of Man.\"],[\"t\",\"  \"],[\"v\",\"14\"],[\"wj\",\"And \"],[\"wj\",\"as Moses lifted up the serpent in the wilderness, so must the Son of Man \"],[\"wj\",\"be lifted up,\"],[\"t\",\"  \"],[\"v\",\"15\"],[\"wj\",\"that whoever believes \"],[\"wj\",\"in him \"],[\"wj\",\"may have eternal life.\"]]],[\"s1\",\"For God So Loved the World\"],[\"p\",[[\"t\",\"  \"],[\"v\",\"16\"],[\"t\",\"  \"],[\"wj\",\"“For \"],[\"t\",\"  \"],[\"t\",\"  \"],[\"wj\",\"God so loved \"],[\"t\",\"  \"],[\"t\",\"  \"],[\"wj\",\"the world,\"],[\"t\",\"  \"],[\"t\",\"  \"],[\"t\",\"  \"],[\"wj\",\"that he gave his only Son, that whoever believes in him should not \"],[\"t\",\"  \"],[\"t\",\"  \"],[\"wj\",\"perish but have eternal life.\"],[\"t\",\"  \"],[\"v\",\"17\"],[\"wj\",\"For \"],[\"wj\",\"God did not send his Son into the world \"],[\"wj\",\"to condemn the world, but in order that the world might be saved through him.\"],[\"t\",\"  \"],[\"v\",\"18\"],[\"wj\",\"Whoever believes in him is not condemned, but whoever does not believe is condemned already, because he has not \"],[\"wj\",\"believed in the name of the only Son of God.\"],[\"t\",\"  \"],[\"v\",\"19\"],[\"wj\",\"And this is the judgment: \"],[\"wj\",\"the light has come into the world, and \"],[\"wj\",\"people loved the darkness rather than the light because \"],[\"wj\",\"their works were evil.\"],[\"t\",\"  \"],[\"v\",\"20\"],[\"wj\",\"For everyone who does wicked things hates the light and does not come to the light, \"],[\"wj\",\"lest his works should be exposed.\"],[\"t\",\"  \"],[\"v\",\"21\"],[\"wj\",\"But whoever \"],[\"wj\",\"does what is true \"],[\"wj\",\"comes to the light, so that it may be clearly seen that his works have been carried out in God.”\"]]],[\"s1\",\"John the Baptist Exalts Christ\"],[\"p\",[[\"t\",\"  \"],[\"v\",\"22\"],[\"t\",\"After this Jesus and his disciples went into the Judean countryside, and he remained there with them and \"],[\"t\",\"was baptizing. \"],[\"v\",\"23\"],[\"t\",\"John also was baptizing at Aenon near Salim, because water was plentiful there, and people were coming and being baptized \"],[\"v\",\"24\"],[\"t\",\"(for \"],[\"t\",\"John had not yet been put in prison).\"]]],[\"p\",[[\"t\",\"  \"],[\"v\",\"25\"],[\"t\",\"Now a discussion arose between some of John’s disciples and a Jew over \"],[\"t\",\"purification. \"],[\"v\",\"26\"],[\"t\",\"And they came to John and said to him, \"],[\"t\",\"“Rabbi, he who was with you across the Jordan, \"],[\"t\",\"to whom you bore witness—look, he is baptizing, and \"],[\"t\",\"all are going to him.” \"],[\"v\",\"27\"],[\"t\",\"John answered, \"],[\"t\",\"“A person cannot receive even one thing \"],[\"t\",\"unless it is given him \"],[\"t\",\"from heaven. \"],[\"v\",\"28\"],[\"t\",\"You yourselves bear me witness, that I said, \"],[\"t\",\"‘I am not the Christ, but \"],[\"t\",\"I have been sent before him.’ \"],[\"v\",\"29\"],[\"t\",\"The one who has the bride is the bridegroom. \"],[\"t\",\"The friend of the bridegroom, who stands and hears him, \"],[\"t\",\"rejoices greatly at the bridegroom’s voice. Therefore this joy of mine is now complete. \"],[\"v\",\"30\"],[\"t\",\"He must increase, but I must decrease.”\"]]],[\"p\",[[\"t\",\"  \"],[\"v\",\"31\"],[\"t\",\"  \"],[\"t\",\"He who comes from above \"],[\"t\",\"is above all. He who is of the earth belongs to the earth and \"],[\"t\",\"speaks in an earthly way. \"],[\"t\",\"He who comes from heaven \"],[\"t\",\"is above all. \"],[\"v\",\"32\"],[\"t\",\"He bears witness to what he has seen and heard, \"],[\"t\",\"yet no one receives his testimony. \"],[\"v\",\"33\"],[\"t\",\"Whoever receives his testimony \"],[\"t\",\"sets his seal to this, \"],[\"t\",\"that God is true. \"],[\"v\",\"34\"],[\"t\",\"For he whom \"],[\"t\",\"God has sent utters the words of God, for he gives the Spirit \"],[\"t\",\"without measure. \"],[\"v\",\"35\"],[\"t\",\"The Father loves the Son and \"],[\"t\",\"has given all things into his hand. \"],[\"v\",\"36\"],[\"t\",\"Whoever believes in the Son has eternal life; \"],[\"t\",\"whoever does not obey the Son shall not \"],[\"t\",\"see life, but the wrath of God remains on him.\"]]]]"
        ),
    )
}
