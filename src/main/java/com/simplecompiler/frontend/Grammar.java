package com.simplecompiler.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.parboiled.Action;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.Rule;

/**
 * @author Dmitry
 */
public class Grammar extends BaseParser<Object> {

    public Rule start() {
        return Sequence(whitespace(), Sequence(push(new ListStartMark()), OneOrMore(sexpr()), collectToList()), whitespace(), EOI);
    }

    Rule sexpr() {
        return Sequence(
                whitespace(),
                FirstOf(
                        list(),
                        atom()
                )
        );
    }

    Rule list() {
        return Sequence(
                "(",
                push(new ListStartMark()),
                ZeroOrMore(
                        sexpr()
                ),
                whitespace(),
                ")",
                collectToList()
        );
    }

    Rule atom() {
        return Sequence(
                whitespace(),
                FirstOf(
                        number(),
                        symbol()
                )
        );
    }

    Rule symbol() {
        return Sequence(
                Sequence(
                        letter(),
                        ZeroOrMore(
                                FirstOf(
                                        letter(),
                                        digit()
                                )
                        )
                ),
                push(match()));
    }

    Rule number() {
        return FirstOf(
                hexNumber(),
                binNumber(),
                regularNumber()
        );
    }

    Rule regularNumber() {
        return Sequence(
                Sequence(
                        Optional("-"),
                        OneOrMore(
                                digit()
                        )
                ),
                push(match())
        );
    }

    Rule hexNumber() {
        return Sequence(
                Sequence(
                        "0x",
                        OneOrMore(
                                FirstOf(
                                        digit(),
                                        "A", "B", "C", "D", "E", "F",
                                        "a", "b", "c", "d", "e", "f"
                                )
                        )
                ),
                push(
                        String.valueOf(Integer.parseInt(match().substring(2), 16))
                )
        );
    }

    Rule binNumber() {
        return Sequence(
                Sequence(
                        "0b",
                        OneOrMore(
                                FirstOf(
                                        "0", "1"
                                )
                        )
                ),
                push(
                        String.valueOf(Integer.parseInt(match().substring(2), 2))
                )
        );
    }

    Rule digit() {
        return CharRange('0', '9');
    }

    Rule letter() {
        return FirstOf(
                CharRange('a', 'z'),
                CharRange('A', 'Z'),
                CharRange('0', '9'),
                "~", "|", "-", "+", "!", "\\", "$", "%", "&", "*",
                ".", "/", ":", "<", "=", ">", "?", "@", "^", "_"
        );
    }

    Rule letterWithoutNumbers() {
        return FirstOf(
                CharRange('a', 'z'),
                CharRange('A', 'Z'),
                "~", "|", "-", "+", "!", "\\", "$", "%", "&", "*",
                ".", "/", ":", "<", "=", ">", "?", "@", "^", "_"
        );
    }

    Rule whitespace() {
        return ZeroOrMore(
                FirstOf(
                        blank(),
                        comment()
                )
        );
    }

    Rule blank() {
        return FirstOf(" ", "\t", "\n", "\r");
    }

    Rule comment() {
        return Sequence(
                ";",
                ZeroOrMore(
                        Sequence(
                                TestNot(eol()),
                                ANY
                        )
                )
        );
    }

    Rule eol() {
        return FirstOf(
                Sequence("\n", Optional("\r")),
                Sequence("\r", Optional("\n"))
        );
    }

    protected static class ListStartMark {
    };

    public Action collectToList() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<Object> listOfObjects = new ArrayList<>();
                while (true) {
                    Object value = context.getValueStack().pop();
                    if (value == null) {
                        throw new IllegalStateException("Value stack returned null but should not.");
                    }

                    if (value instanceof ListStartMark) {
                        Collections.reverse(listOfObjects);
                        push(listOfObjects);
                        break;
                    }

                    listOfObjects.add(value);
                }
                return true;
            }
        };
    }
}
