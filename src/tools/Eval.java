
package tools;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

public final class Eval {
    private final Operation rootOperation;

    public Eval(String expression) {
        this.rootOperation = (new Compiler(expression)).compile();
    }

    public BigDecimal eval(Map<String, BigDecimal> variables) {
        return this.rootOperation.eval(variables);
    }

    public BigDecimal eval() {
        return this.eval((Map)null);
    }

    public static BigDecimal eval(String expression, Map<String, BigDecimal> variables) {
        return (new Eval(expression)).eval(variables);
    }

    public static BigDecimal eval(String expression) {
        return (new Eval(expression)).eval();
    }

    public String toString() {
        return this.rootOperation.toString();
    }

    public static class Compiler {
        private final Tokeniser tokeniser;

        Compiler(String expression) {
            this.tokeniser = new Tokeniser(expression);
        }

        Operation compile() {
            Object expression = this.compile((Object)null, (Operator)null, 0, '\u0000', -1);
            return expression instanceof Operation ? (Operation)expression : Eval.Operation.nopOperationfactory(expression);
        }

        private Object compile(Object preReadOperand, Operator preReadOperator, int nestingLevel, char endOfExpressionChar, int terminatePrecedence) {
            Object operand = preReadOperand != null ? preReadOperand : this.getOperand(nestingLevel);
            Operator operator = preReadOperator != null ? preReadOperator : this.tokeniser.getOperator(endOfExpressionChar);

            while(operator != Eval.Operator.END) {
                Object nextOperand;
                if (operator == Eval.Operator.TERNARY) {
                    nextOperand = this.compile((Object)null, (Operator)null, nestingLevel, ':', -1);
                    Object operand3 = this.compile((Object)null, (Operator)null, nestingLevel, endOfExpressionChar, -1);
                    operand = Eval.Operation.tenaryOperationFactory(operator, operand, nextOperand, operand3);
                    operator = Eval.Operator.END;
                } else {
                    nextOperand = this.getOperand(nestingLevel);
                    Operator nextOperator = this.tokeniser.getOperator(endOfExpressionChar);
                    if (nextOperator == Eval.Operator.END) {
                        operand = Eval.Operation.binaryOperationfactory(operator, operand, nextOperand);
                        operator = Eval.Operator.END;
                        if (preReadOperator != null && endOfExpressionChar != 0) {
                            this.tokeniser.pushBack(Eval.Operator.END);
                        }
                    } else if (nextOperator.precedence <= terminatePrecedence) {
                        operand = Eval.Operation.binaryOperationfactory(operator, operand, nextOperand);
                        this.tokeniser.pushBack(nextOperator);
                        operator = Eval.Operator.END;
                    } else if (operator.precedence >= nextOperator.precedence) {
                        operand = Eval.Operation.binaryOperationfactory(operator, operand, nextOperand);
                        operator = nextOperator;
                    } else {
                        operand = Eval.Operation.binaryOperationfactory(operator, operand, this.compile(nextOperand, nextOperator, nestingLevel, endOfExpressionChar, operator.precedence));
                        operator = this.tokeniser.getOperator(endOfExpressionChar);
                        if (operator == Eval.Operator.END && preReadOperator != null && endOfExpressionChar != 0) {
                            this.tokeniser.pushBack(Eval.Operator.END);
                        }
                    }
                }
            }

            return operand;
        }

        private Object getOperand(int nestingLevel) {
            Object operand = this.tokeniser.getOperand();
            if (operand == Eval.Tokeniser.START_NEW_EXPRESSION) {
                operand = this.compile((Object)null, (Operator)null, nestingLevel + 1, ')', -1);
            } else if (operand instanceof Operator) {
                return Eval.Operation.unaryOperationfactory((Operator)operand, this.getOperand(nestingLevel));
            }

            return operand;
        }
    }

    public static final class Operation {
        final Type type;
        final Operator operator;
        final Object operand1;
        final Object operand2;
        final Object operand3;

        private Operation(Type type, Operator operator, Object operand1, Object operand2, Object operand3) {
            this.type = type;
            this.operator = operator;
            this.operand1 = operand1;
            this.operand2 = operand2;
            this.operand3 = operand3;
        }

        static Operation nopOperationfactory(Object operand) {
            return new Operation(Eval.Operator.NOP.resultType, Eval.Operator.NOP, operand, (Object)null, (Object)null);
        }

        static Object unaryOperationfactory(Operator operator, Object operand) {
            validateOperandType(operand, operator.operandType);
            return operand instanceof BigDecimal ? operator.perform((BigDecimal)operand, (BigDecimal)null, (BigDecimal)null) : new Operation(operator.resultType, operator, operand, (Object)null, (Object)null);
        }

        static Object binaryOperationfactory(Operator operator, Object operand1, Object operand2) {
            validateOperandType(operand1, operator.operandType);
            validateOperandType(operand2, operator.operandType);
            return operand1 instanceof BigDecimal && operand2 instanceof BigDecimal ? operator.perform((BigDecimal)operand1, (BigDecimal)operand2, (BigDecimal)null) : new Operation(operator.resultType, operator, operand1, operand2, (Object)null);
        }

        static Object tenaryOperationFactory(Operator operator, Object operand1, Object operand2, Object operand3) {
            validateOperandType(operand1, Eval.Type.BOOLEAN);
            validateOperandType(operand2, Eval.Type.ARITHMETIC);
            validateOperandType(operand3, Eval.Type.ARITHMETIC);
            if (operand1 instanceof BigDecimal) {
                return ((BigDecimal)operand1).signum() != 0 ? operand2 : operand3;
            } else {
                return new Operation(Eval.Type.ARITHMETIC, operator, operand1, operand2, operand3);
            }
        }

        BigDecimal eval(Map<String, BigDecimal> variables) {
            switch (this.operator.numberOfOperands) {
                case 2:
                    return this.operator.perform(this.evaluateOperand(this.operand1, variables), this.evaluateOperand(this.operand2, variables), (BigDecimal)null);
                case 3:
                    return this.operator.perform(this.evaluateOperand(this.operand1, variables), this.evaluateOperand(this.operand2, variables), this.evaluateOperand(this.operand3, variables));
                default:
                    return this.operator.perform(this.evaluateOperand(this.operand1, variables), (BigDecimal)null, (BigDecimal)null);
            }
        }

        private BigDecimal evaluateOperand(Object operand, Map<String, BigDecimal> variables) {
            if (operand instanceof Operation) {
                return ((Operation)operand).eval(variables);
            } else if (operand instanceof String) {
                BigDecimal value;
                if (variables != null && (value = (BigDecimal)variables.get(operand)) != null) {
                    return value;
                } else {
                    throw new RuntimeException("no value for variable \"" + operand + "\"");
                }
            } else {
                return (BigDecimal)operand;
            }
        }

        private static void validateOperandType(Object operand, Type type) {
            Type operandType;
            if (operand instanceof Operation && (operandType = ((Operation)operand).type) != type) {
                throw new RuntimeException("cannot use " + operandType.name + " operands with " + type.name + " operators");
            }
        }

        public String toString() {
            switch (this.operator.numberOfOperands) {
                case 2:
                    return "(" + this.operand1 + this.operator.string + this.operand2 + ")";
                case 3:
                    return "(" + this.operand1 + this.operator.string + this.operand2 + ":" + this.operand3 + ")";
                default:
                    return "(" + this.operator.string + this.operand1 + ")";
            }
        }
    }

    public static enum Operator {
        END(-1, 0, (String)null, (Type)null, (Type)null) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                throw new RuntimeException("END is a dummy operation");
            }
        },
        TERNARY(0, 3, "?", (Type)null, (Type)null) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.signum() != 0 ? value2 : value3;
            }
        },
        AND(0, 2, "&&", Eval.Type.BOOLEAN, Eval.Type.BOOLEAN) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.signum() != 0 && value2.signum() != 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        },
        OR(0, 2, "||", Eval.Type.BOOLEAN, Eval.Type.BOOLEAN) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.signum() == 0 && value2.signum() == 0 ? BigDecimal.ZERO : BigDecimal.ONE;
            }
        },
        GT(1, 2, ">", Eval.Type.BOOLEAN, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.compareTo(value2) > 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        },
        GE(1, 2, ">=", Eval.Type.BOOLEAN, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.compareTo(value2) >= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        },
        LT(1, 2, "<", Eval.Type.BOOLEAN, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.compareTo(value2) < 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        },
        LE(1, 2, "<=", Eval.Type.BOOLEAN, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.compareTo(value2) <= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        },
        EQ(1, 2, "==", Eval.Type.BOOLEAN, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.compareTo(value2) == 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        },
        NE(1, 2, "!=", Eval.Type.BOOLEAN, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.compareTo(value2) != 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        },
        ADD(2, 2, "+", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.add(value2);
            }
        },
        SUB(2, 2, "-", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.subtract(value2);
            }
        },
        DIV(3, 2, "/", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.divide(value2, MathContext.DECIMAL128);
            }
        },
        REMAINDER(3, 2, "%", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.remainder(value2, MathContext.DECIMAL128);
            }
        },
        MUL(3, 2, "*", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.multiply(value2);
            }
        },
        NEG(4, 1, "-", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.negate();
            }
        },
        PLUS(4, 1, "+", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1;
            }
        },
        ABS(4, 1, " abs ", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1.abs();
            }
        },
        POW(4, 2, " pow ", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                try {
                    return value1.pow(value2.intValueExact());
                } catch (ArithmeticException var5) {
                    throw new RuntimeException("pow argument: " + var5.getMessage());
                }
            }
        },
        INT(4, 1, "int ", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return new BigDecimal(value1.toBigInteger());
            }
        },
        CEIL(4, 1, "ceil ", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return new BigDecimal(Math.ceil(value1.doubleValue()));
            }
        },
        FLOOR(4, 1, "floor ", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return new BigDecimal(Math.floor(value1.doubleValue()));
            }
        },
        NOP(4, 1, "", Eval.Type.ARITHMETIC, Eval.Type.ARITHMETIC) {
            BigDecimal perform(BigDecimal value1, BigDecimal value2, BigDecimal value3) {
                return value1;
            }
        };

        final int precedence;
        final int numberOfOperands;
        final String string;
        final Type resultType;
        final Type operandType;

        private Operator(int precedence, int numberOfOperands, String string, Type resultType, Type operandType) {
            this.precedence = precedence;
            this.numberOfOperands = numberOfOperands;
            this.string = string;
            this.resultType = resultType;
            this.operandType = operandType;
        }

        abstract BigDecimal perform(BigDecimal var1, BigDecimal var2, BigDecimal var3);
    }

    public static final class Tokeniser {
        static final Character START_NEW_EXPRESSION = '(';
        private final String string;
        private int position;
        private Operator pushedBackOperator = null;

        Tokeniser(String string) {
            this.string = string;
            this.position = 0;
        }

        int getPosition() {
            return this.position;
        }

        void setPosition(int position) {
            this.position = position;
        }

        void pushBack(Operator operator) {
            this.pushedBackOperator = operator;
        }

        Operator getOperator(char endOfExpressionChar) {
            if (this.pushedBackOperator != null) {
                Operator operator = this.pushedBackOperator;
                this.pushedBackOperator = null;
                return operator;
            } else {
                int len = this.string.length();

                char ch;
                for(ch = 0; this.position < len && Character.isWhitespace(ch = this.string.charAt(this.position)); ++this.position) {
                }

                if (this.position == len) {
                    if (endOfExpressionChar == 0) {
                        return Eval.Operator.END;
                    } else {
                        throw new RuntimeException("missing " + endOfExpressionChar);
                    }
                } else {
                    ++this.position;
                    if (ch == endOfExpressionChar) {
                        return Eval.Operator.END;
                    } else {
                        switch (ch) {
                            case '!':
                                if (this.position < len && this.string.charAt(this.position) == '=') {
                                    ++this.position;
                                    return Eval.Operator.NE;
                                }

                                throw new RuntimeException("use != or <> for inequality at position " + this.position);
                            case '%':
                                return Eval.Operator.REMAINDER;
                            case '&':
                                if (this.position < len && this.string.charAt(this.position) == '&') {
                                    ++this.position;
                                    return Eval.Operator.AND;
                                }

                                throw new RuntimeException("use && for AND at position " + this.position);
                            case '*':
                                return Eval.Operator.MUL;
                            case '+':
                                return Eval.Operator.ADD;
                            case '-':
                                return Eval.Operator.SUB;
                            case '/':
                                return Eval.Operator.DIV;
                            case '<':
                                if (this.position < len) {
                                    switch (this.string.charAt(this.position)) {
                                        case '=':
                                            ++this.position;
                                            return Eval.Operator.LE;
                                        case '>':
                                            ++this.position;
                                            return Eval.Operator.NE;
                                    }
                                }

                                return Eval.Operator.LT;
                            case '=':
                                if (this.position < len && this.string.charAt(this.position) == '=') {
                                    ++this.position;
                                    return Eval.Operator.EQ;
                                }

                                throw new RuntimeException("use == for equality at position " + this.position);
                            case '>':
                                if (this.position < len && this.string.charAt(this.position) == '=') {
                                    ++this.position;
                                    return Eval.Operator.GE;
                                }

                                return Eval.Operator.GT;
                            case '?':
                                return Eval.Operator.TERNARY;
                            case '|':
                                if (this.position < len && this.string.charAt(this.position) == '|') {
                                    ++this.position;
                                    return Eval.Operator.OR;
                                }

                                throw new RuntimeException("use || for OR at position " + this.position);
                            default:
                                if (Character.isUnicodeIdentifierStart(ch)) {
                                    int start;
                                    for(start = this.position - 1; this.position < len && Character.isUnicodeIdentifierPart(this.string.charAt(this.position)); ++this.position) {
                                    }

                                    String name = this.string.substring(start, this.position);
                                    if (name.equals("pow")) {
                                        return Eval.Operator.POW;
                                    }
                                }

                                throw new RuntimeException("operator expected at position " + this.position + " instead of '" + ch + "'");
                        }
                    }
                }
            }
        }

        Object getOperand() {
            int len = this.string.length();

            char ch;
            for(ch = 0; this.position < len && Character.isWhitespace(ch = this.string.charAt(this.position)); ++this.position) {
            }

            if (this.position == len) {
                throw new RuntimeException("operand expected but end of string found");
            } else if (ch == '(') {
                ++this.position;
                return START_NEW_EXPRESSION;
            } else if (ch == '-') {
                ++this.position;
                return Eval.Operator.NEG;
            } else if (ch == '+') {
                ++this.position;
                return Eval.Operator.PLUS;
            } else if (ch != '.' && !Character.isDigit(ch)) {
                if (!Character.isUnicodeIdentifierStart(ch)) {
                    throw new RuntimeException("operand expected but '" + ch + "' found");
                } else {
                    int start;
                    for(start = this.position++; this.position < len && Character.isUnicodeIdentifierPart(this.string.charAt(this.position)); ++this.position) {
                    }

                    String name = this.string.substring(start, this.position);
                    if (name.equals("abs")) {
                        return Eval.Operator.ABS;
                    } else if (name.equals("int")) {
                        return Eval.Operator.INT;
                    } else if (name.equals("ceil")) {
                        return Eval.Operator.CEIL;
                    } else {
                        return name.equals("floor") ? Eval.Operator.FLOOR : name;
                    }
                }
            } else {
                return this.getBigDecimal();
            }
        }

        private BigDecimal getBigDecimal() {
            int len = this.string.length();

            int start;
            char ch;
            for(start = this.position; this.position < len && (Character.isDigit(ch = this.string.charAt(this.position)) || ch == '.'); ++this.position) {
            }

            if (this.position < len && ((ch = this.string.charAt(this.position)) == 'E' || ch == 'e')) {
                ++this.position;
                if (this.position < len && ((ch = this.string.charAt(this.position)) == '+' || ch == '-')) {
                    ++this.position;
                }

                while(this.position < len && Character.isDigit(this.string.charAt(this.position))) {
                    ++this.position;
                }
            }

            return new BigDecimal(this.string.substring(start, this.position));
        }

        public String toString() {
            return this.string.substring(0, this.position) + ">>>" + this.string.substring(this.position);
        }
    }

    public static enum Type {
        ARITHMETIC("arithmetic"),
        BOOLEAN("boolean");

        final String name;

        private Type(String name) {
            this.name = name;
        }
    }
}
