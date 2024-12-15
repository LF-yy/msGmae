//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package constants;

import client.MapleJob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.SearchGenerator;
import tools.SearchGenerator.SearchType;

public class SkillConstants {
    private static int[][] initialMasterLevelList = new int[][]{{1003, 1}, {1004, 1}, {1005, 1}, {8, 1}, {1000, 3}, {1001, 3}, {1002, 3}, {10000012, 20}, {20001000, 3}, {20001001, 3}, {20001002, 3}, {20001003, 3}, {20001004, 1}, {20001005, 1}, {20000024, 1}, {20000012, 20}, {21000000, 10}, {21001001, 15}, {21000002, 20}, {21001003, 20}, {21100000, 20}, {21100001, 20}, {21100002, 30}, {21101003, 20}, {21100004, 20}, {21100005, 30}, {21110000, 20}, {21111001, 20}, {21110002, 20}, {21110003, 30}, {21110004, 30}, {21111005, 20}, {21110006, 20}, {21110007, 20}, {21110008, 20}, {21121000, 30}, {21120001, 30}, {21120002, 30}, {21121003, 30}, {21120004, 30}, {21120005, 30}, {21120006, 30}, {21120007, 30}, {21121008, 5}, {9001000, 1}, {9001001, 1}, {9001002, 1}, {9001003, 1}, {9001004, 1}, {9001005, 1}, {9001006, 1}, {9001007, 1}, {9001008, 1}, {9001009, 1}, {1121011, 1}, {1221012, 1}, {1321010, 1}, {2121008, 1}, {2221008, 1}, {2321009, 1}, {3121009, 1}, {3221008, 1}, {4121009, 1}, {4221008, 1}, {1000002, 8}, {3000002, 8}, {4000001, 8}, {1000001, 10}, {2000001, 10}, {1000000, 16}, {2000000, 16}, {3000000, 16}, {1001003, 20}, {3200001, 30}, {1001004, 20}, {1001005, 20}, {2001002, 20}, {2001003, 20}, {2001004, 20}, {2001005, 20}, {3000001, 20}, {3001003, 20}, {3001004, 20}, {3001005, 20}, {4000000, 20}, {4001344, 20}, {4001334, 20}, {4001002, 20}, {4001003, 20}, {1101005, 20}, {1100001, 20}, {1100000, 20}, {1200001, 20}, {1200000, 20}, {1300000, 20}, {1300001, 20}, {3100000, 20}, {3200000, 20}, {4100000, 20}, {4200000, 20}, {4201002, 20}, {4101003, 20}, {3201002, 20}, {3101002, 20}, {1301004, 20}, {1301005, 20}, {1201004, 20}, {1201005, 20}, {1101004, 20}, {1101006, 20}, {1201006, 20}, {1301006, 20}, {2101001, 20}, {2100000, 20}, {2101003, 20}, {2101002, 20}, {2201001, 20}, {2200000, 20}, {2201003, 20}, {2201002, 20}, {2301004, 20}, {2301003, 20}, {2300000, 20}, {2301001, 20}, {3101003, 20}, {3101004, 20}, {3201003, 20}, {3201004, 20}, {4100002, 20}, {4101004, 20}, {4200001, 20}, {4201003, 20}, {4211005, 20}, {4211003, 20}, {4210000, 20}, {4110000, 20}, {4111001, 20}, {4111003, 20}, {3210000, 20}, {3110000, 20}, {3210001, 20}, {3110001, 20}, {3211002, 20}, {3111002, 20}, {2210000, 20}, {2211004, 20}, {2211005, 20}, {2111005, 20}, {2111004, 20}, {2110000, 20}, {2311001, 20}, {2311005, 30}, {2310000, 20}, {1311007, 20}, {1310000, 20}, {1311008, 20}, {1210001, 20}, {1211009, 20}, {1210000, 20}, {1110001, 20}, {1111007, 20}, {1110000, 20}, {1121000, 20}, {1221000, 20}, {1321000, 20}, {2121000, 20}, {2221000, 20}, {2321000, 20}, {3121000, 20}, {3221000, 20}, {4121000, 20}, {4221000, 20}, {1321007, 10}, {1320009, 25}, {1320008, 25}, {2321006, 10}, {1220010, 10}, {1221004, 25}, {1221003, 25}, {1100003, 30}, {1100002, 30}, {1101007, 30}, {1200003, 30}, {1200002, 30}, {1201007, 30}, {1300003, 30}, {1300002, 30}, {1301007, 30}, {2101004, 30}, {2101005, 30}, {2201004, 30}, {2201005, 30}, {2301002, 30}, {2301005, 30}, {3101005, 30}, {3201005, 30}, {4100001, 30}, {4101005, 30}, {4201005, 30}, {4201004, 30}, {1111006, 30}, {1111005, 30}, {1111002, 30}, {1111004, 30}, {1111003, 30}, {1111008, 30}, {1211006, 30}, {1211002, 30}, {1211004, 30}, {1211003, 30}, {1211005, 30}, {1211008, 30}, {1211007, 30}, {1311004, 30}, {1311003, 30}, {1311006, 30}, {1311002, 30}, {1311005, 30}, {1311001, 30}, {2110001, 30}, {2111006, 30}, {2111002, 30}, {2111003, 30}, {2210001, 30}, {2211006, 30}, {2211002, 30}, {2211003, 30}, {2311003, 30}, {2311002, 30}, {2311004, 30}, {2311006, 30}, {3111004, 30}, {3111003, 30}, {3111005, 30}, {3111006, 30}, {3211004, 30}, {3211003, 30}, {3211005, 30}, {3211006, 30}, {4111005, 30}, {4111006, 20}, {4111004, 30}, {4111002, 30}, {4211002, 30}, {4211004, 30}, {4211001, 30}, {4211006, 30}, {1120004, 30}, {1120003, 30}, {1120005, 30}, {1121000, 30}, {1121008, 30}, {1121010, 30}, {1121011, 5}, {1121006, 30}, {1121002, 30}, {1220005, 30}, {1221009, 30}, {1220006, 30}, {1221007, 30}, {1221011, 30}, {1221012, 5}, {1221002, 30}, {1221000, 30}, {1320005, 30}, {1320006, 30}, {1321003, 30}, {1321002, 30}, {1321000, 30}, {1321010, 5}, {2121005, 30}, {2121003, 30}, {2121004, 30}, {2121002, 30}, {2121007, 30}, {2121006, 30}, {2121008, 5}, {2121000, 30}, {2221007, 30}, {2221006, 30}, {2221003, 30}, {2221005, 30}, {2221004, 30}, {2221002, 30}, {2221008, 5}, {2221000, 30}, {2321007, 30}, {2321003, 30}, {2321008, 30}, {2321009, 5}, {2321005, 30}, {2321004, 30}, {2321000, 30}, {2321002, 30}, {3120005, 30}, {3121008, 30}, {3121000, 30}, {3121009, 5}, {3121003, 30}, {3121007, 30}, {3121006, 30}, {3121000, 30}, {3121002, 30}, {3121004, 30}, {3221006, 30}, {3221000, 30}, {3220004, 30}, {3221000, 30}, {3221003, 30}, {3221005, 30}, {3221001, 30}, {3221002, 30}, {3221007, 30}, {3221008, 5}, {4121004, 30}, {4121008, 30}, {4121000, 30}, {4121003, 30}, {4121006, 30}, {4121007, 30}, {4121000, 30}, {4121009, 5}, {4120005, 30}, {4221001, 30}, {4221007, 30}, {4221004, 30}, {4221000, 30}, {4221000, 30}, {4221003, 30}, {4221006, 30}, {4221008, 5}, {4220005, 30}, {1321001, 30}, {4120002, 30}, {2221001, 30}, {3100001, 30}, {1121001, 30}, {1221001, 30}, {2121001, 30}, {2221001, 30}, {2321001, 30}, {4220002, 30}, {8, 1}, {5000000, 20}, {5001001, 20}, {5001002, 20}, {5001003, 20}, {5001005, 10}, {5100000, 10}, {5100001, 20}, {5101002, 20}, {5101003, 20}, {5101004, 20}, {5101005, 10}, {5101006, 20}, {5101007, 10}, {5200000, 20}, {5201001, 20}, {5201002, 20}, {5201003, 20}, {5201004, 20}, {5201005, 10}, {5201006, 20}, {5110000, 20}, {5110001, 40}, {5111002, 30}, {5111004, 20}, {5111005, 20}, {5210000, 20}, {5211001, 30}, {5211002, 30}, {5211004, 30}, {5211005, 30}, {5211006, 30}, {5121000, 30}, {5121001, 30}, {5121002, 30}, {5121003, 20}, {5121004, 30}, {5121005, 30}, {5111006, 30}, {5121007, 30}, {5121008, 30}, {5121009, 20}, {5121010, 30}, {5221000, 30}, {5220001, 30}, {5220002, 20}, {5221003, 30}, {5221004, 30}, {5221006, 10}, {5221007, 30}, {5221008, 30}, {5221009, 20}, {5221010, 25}, {5220011, 20}, {11000000, 10}, {11001001, 10}, {11001002, 20}, {11001003, 20}, {11001004, 20}, {11100000, 20}, {11101001, 20}, {11101002, 30}, {11101003, 20}, {11101004, 30}, {11101005, 10}, {11110000, 20}, {11111001, 20}, {11111002, 20}, {11111003, 20}, {11111004, 30}, {11110005, 20}, {11111006, 30}, {11111007, 20}, {12000000, 10}, {12001001, 10}, {12001002, 10}, {12001003, 20}, {12001004, 20}, {12101000, 20}, {12101001, 20}, {12101002, 20}, {12101003, 20}, {12101004, 20}, {12101005, 20}, {12101006, 20}, {12110000, 20}, {12110001, 20}, {12111002, 20}, {12111003, 20}, {12111004, 20}, {12111005, 30}, {12111006, 30}, {13000000, 20}, {13000001, 8}, {13001002, 10}, {13001003, 20}, {13001004, 20}, {13100000, 20}, {13101001, 20}, {13101002, 30}, {13101003, 20}, {13100004, 20}, {13101005, 20}, {13101006, 10}, {13111000, 20}, {13111001, 30}, {13111002, 20}, {13110003, 20}, {13111004, 20}, {13111005, 10}, {13111006, 20}, {13111007, 20}, {14000000, 10}, {14000001, 8}, {14001002, 10}, {14001003, 10}, {14001004, 20}, {14001005, 20}, {14100000, 20}, {14100001, 30}, {14101002, 20}, {14101003, 20}, {14101004, 20}, {14100005, 10}, {14101006, 20}, {14111000, 30}, {14111001, 20}, {14111002, 30}, {14110003, 20}, {14110004, 20}, {14111005, 20}, {14111006, 30}, {15000000, 10}, {15001001, 20}, {15001002, 20}, {15001003, 10}, {15001004, 20}, {15100000, 10}, {15100001, 20}, {15101002, 20}, {15101003, 20}, {15100004, 20}, {15101005, 20}, {15101006, 20}, {15110000, 20}, {15111001, 20}, {15111002, 10}, {15111003, 20}, {15111004, 20}, {15111005, 20}, {15111006, 20}, {15111007, 30}, {8, 1}};

    public SkillConstants() {
    }

    public static boolean isSkill92XX0000(int skillId) {
        return skillId / 1000000 == 92 && skillId % 10000 == 0;
    }

    public static boolean isSkill92XX____(int skillId) {
        return !isSkill92XX0000(skillId) && isSkill92XX0000(10000 * (skillId / 10000));
    }

    public static boolean is4thNotNeedMasterLevel(int skillId) {
        if (skillId > 5220014) {
            if (skillId > 23120011) {
                if (skillId != 23120013 && skillId != 23121008 && skillId != 33120010 && skillId != 35120014) {
                    return skillId == 51120000;
                } else {
                    return true;
                }
            } else if (skillId != 23120011 && skillId != 5320007 && skillId != 5321004 && skillId != 5321006 && skillId != 21120011 && skillId != 21120014) {
                return skillId == 22181004;
            } else {
                return true;
            }
        } else if (skillId == 5220014) {
            return true;
        } else if (skillId <= 4110012) {
            if (skillId != 4110012 && skillId != 1120012 && skillId != 1320011 && skillId != 2121009 && skillId != 2221009 && skillId != 2321010) {
                return skillId == 3210015;
            } else {
                return true;
            }
        } else {
            if (skillId != 4210012 && skillId != 4340010 && skillId != 4340012) {
                if (skillId <= 5120010) {
                    return false;
                }

                if (skillId > 5120012) {
                    return skillId == 5220012;
                }
            }

            return true;
        }
    }

    public static boolean isNot4thNeedMasterLevel(int skillId) {
        if (skillId > 101100101) {
            if (skillId != 101100201 && skillId != 101110102 && skillId != 101110200 && skillId != 101110203 && skillId != 101120104) {
                return skillId == 101120204;
            } else {
                return true;
            }
        } else if (skillId != 101100101 && skillId != 4311003 && skillId != 4321006 && skillId != 4330009 && skillId != 4331002 && skillId != 4341004 && skillId != 4341007) {
            return skillId == 101000101;
        } else {
            return true;
        }
    }

    public static boolean isSkillNeedMasterLevel(int skillId) {
        if (is4thNotNeedMasterLevel(skillId)) {
            return false;
        } else if (isSkill92XX0000(skillId)) {
            return false;
        } else if (isSkill92XX____(skillId)) {
            return false;
        } else if (MapleJob.isJob8000(skillId)) {
            return false;
        } else {
            int jobid = getJobBySkill(skillId);
            if (!MapleJob.is初心者(jobid) && !MapleJob.isJob9500(skillId) && skillId != 42120024) {
                int jobTimes = MapleJob.get转数(jobid);
                if (isNot4thNeedMasterLevel(skillId)) {
                    return true;
                } else {
                    return jobTimes == 4;
                }
            } else {
                return false;
            }
        }
    }

    public static int get紫扇传授UnknownValue(int skillId) {
        byte result;
        if (skillId != 40020002 && skillId != 80000004) {
            result = 0;
        } else {
            result = 100;
        }

        return result;
    }

    public static int getJobBySkill(int skillId) {
        int result = skillId / 10000;
        if (skillId / 10000 == 8000) {
            result = skillId / 100;
        }

        return result;
    }

    public static boolean isApplicableSkill(int skil) {
        return (skil < 80000000 || skil >= 100000000) && (skil % 10000 < 8000 || skil % 10000 > 8006) && !isAngel(skil) || skil >= 92000000 || skil >= 80000000 && skil < 80010000;
    }

    public static boolean isRidingSKill(int skil) {
        return skil >= 80001000 && skil < 80010000;
    }

    public static boolean isAngel(int skillId) {
        if (MapleJob.isBeginner(skillId / 10000) || skillId / 100000 == 800) {
            switch (skillId % 10000) {
                case 86:
                case 1085:
                case 1087:
                case 1090:
                case 1179:
                    return true;
            }
        }

        switch (skillId) {
            case 80000052:
            case 80000053:
            case 80000054:
            case 80000086:
            case 80001154:
            case 80001262:
            case 80001518:
            case 80001519:
            case 80001520:
            case 80001521:
            case 80001522:
            case 80001523:
            case 80001524:
            case 80001525:
            case 80001526:
            case 80001527:
            case 80001528:
            case 80001529:
            case 80001530:
            case 80001715:
            case 80001716:
            case 80001717:
            case 80001718:
            case 80001719:
            case 80001720:
            case 80001721:
            case 80001722:
            case 80001723:
            case 80001724:
            case 80001725:
            case 80001726:
            case 80001727:
                return true;
            default:
                return false;
        }
    }

    public static boolean is紫扇仰波(int id) {
        return id == 42001000 || id > 42001004 && id <= 42001006;
    }

    public static boolean is初心者紫扇仰波(int id) {
        return id == 40021185 || id == 42001006 || id == 80011067;
    }

    public static boolean sub_9F5282(int id) {
        return id == 4221052 || id == 65121052;
    }

    public static boolean sub_9F529C(int id) {
        return id == 13121052 || id - 13121052 == 1000000 || id - 13121052 == 2000000 || id - 13121052 == 66880377 || id - 13121052 == 66880379 || id - 13121052 - 66880379 == 19999852;
    }

    public static boolean isKeyDownSkillWithPos(int id) {
        return id == 13111020 || id == 112111016;
    }

    public static int getHyperAddBullet(int id) {
        if (id == 4121013) {
            return 4120051;
        } else {
            return id == 5321012 ? 5320051 : 0;
        }
    }

    public static int getHyperAddAttack(int id) {
        if (id > 12120011) {
            if (id > 41121001) {
                if (id > 61121100) {
                    if (id > 112101009) {
                        if (id == 112111004) {
                            return 112120050;
                        }

                        if (id > 112119999 && id <= 112120003) {
                            return 112120053;
                        }

                        return 0;
                    }

                    if (id == 112101009) {
                        return 112120048;
                    }

                    if (id != 61121201) {
                        if (id <= 65121006 || id > 65121008 && id != 65121101) {
                            return 0;
                        }

                        return 65120051;
                    }
                } else if (id != 61121100) {
                    switch (id) {
                        case 41121002:
                            return 41120050;
                        case 41121018:
                        case 41121021:
                            return 41120048;
                        case 42121000:
                            return 42120045;
                        case 51121007:
                            return 51120051;
                        case 51121008:
                            return 51120048;
                        default:
                            return 0;
                    }
                }

                return 61120045;
            }

            if (id == 41121001) {
                return 41120044;
            }

            if (id > 21121013) {
                if (id == 22181002) {
                    return 0;
                }

                if (id == 25121005) {
                    return 25120148;
                }

                if (id == 31111005) {
                    return 31120044;
                }

                if (id == 31121001) {
                    return 31120050;
                }

                if (id == 32111003) {
                    return 0;
                }

                if (id == 35121016) {
                    return 35120051;
                }
            } else {
                if (id == 21121013) {
                }

                if (id == 13121002) {
                    return 13120048;
                }

                if (id - 13121002 == 1000000) {
                    return 14120045;
                }

                if (id - 13121002 == 1990020 || id - 13121002 == 1999001) {
                    return 15120045;
                }

                if (id - 13121002 == 2000000) {
                    return 15120048;
                }

                if (id - 13121002 - 2000000 == 5999003) {
                    return 21120047;
                }

                if (id - 13121002 - 2000000 - 5999003 == 1) {
                    return 21120049;
                }
            }
        } else {
            if (id == 12120011) {
                return 12120046;
            }

            if (id <= 5121017) {
                if (id >= 5121016) {
                    return 5120051;
                }

                if (id <= 3121015) {
                    switch (id) {
                        case 1120017:
                        case 1121008:
                            return 1120051;
                        case 1221009:
                            return 1220048;
                        case 1221011:
                            return 1220050;
                        case 2121003:
                            return 2120049;
                        case 2121006:
                            return 2120048;
                        case 2221006:
                            return 2220048;
                        case 3121015:
                            return 3120048;
                        default:
                            return 0;
                    }
                }

                if (id == 3121020) {
                    return 3120051;
                }

                if (id == 3221017) {
                    return 3220048;
                }

                if (id == 4221007) {
                    return 4220048;
                }

                if (id == 4331000) {
                    return 4340045;
                }

                if (id == 4341009) {
                    return 4340048;
                }

                if (id != 5121007) {
                    return 0;
                }

                return 5120048;
            }

            if (id > 5721064) {
                if (id == 11121103 || id - 11121103 == 100) {
                    return 11120048;
                }

                if (id - 11121103 == 878923 || id - 11121103 == 978925 || id - 11121103 == 988925 || id - 11121103 == 998907) {
                    return 12120045;
                }
            } else {
                if (id == 5721064) {
                    return 5720048;
                }

                if (id == 5121020) {
                    return 5120048;
                }

                if (id - 5121020 == 99996) {
                    return 5220047;
                }

                if (id - 5121020 == 198991) {
                }

                if (id - 5121020 == 199980) {
                    return 5320048;
                }

                if (id - 5121020 == 199984) {
                    return 5320043;
                }

                if (id - 5121020 == 600041) {
                    return 5720045;
                }
            }
        }

        return 0;
    }

    public static int SkillIncreaseMobCount(int sk) {
        int inc = 0;
        switch (sk) {
            case 1121008:
            case 1211008:
            case 2211007:
            case 2221006:
            case 2221012:
            case 3121015:
            case 3221017:
            case 4121017:
            case 4221007:
            case 4331000:
            case 4341004:
            case 5121016:
            case 5321000:
            case 5721007:
            case 11121103:
            case 11121203:
            case 12120011:
            case 13121002:
            case 15121002:
            case 24121000:
            case 24121005:
            case 27121202:
            case 27121303:
            case 32121003:
            case 33121002:
            case 35121015:
            case 36121000:
            case 36121011:
            case 36121012:
            case 41121009:
            case 41121018:
            case 41121021:
            case 42121000:
            case 51121008:
            case 61111100:
            case 112001008:
            case 112101009:
            case 112111004:
                inc = 2;
                break;
            case 1201011:
            case 1201012:
            case 1221004:
                inc = 3;
        }

        return inc;
    }

    public static boolean isJobSkill(int skillId, int jobId) {
        switch (jobId) {
            case 100:
                return skillId >= 1000000 && skillId < 1100000;
            case 110:
                return skillId >= 1000000 && skillId < 1110000;
            case 111:
                return skillId >= 1000000 && skillId < 1120000;
            case 112:
                return skillId >= 1000000 && skillId < 1130000;
            case 120:
                return skillId >= 1000000 && skillId < 1100000 || skillId >= 1200000 && skillId < 1210000;
            case 121:
                return skillId >= 1000000 && skillId < 1100000 || skillId >= 1200000 && skillId < 1220000;
            case 122:
                return skillId >= 1000000 && skillId < 1100000 || skillId >= 1200000 && skillId < 1230000;
            case 130:
                return skillId >= 1000000 && skillId < 1100000 || skillId >= 1300000 && skillId < 1310000;
            case 131:
                return skillId >= 1000000 && skillId < 1100000 || skillId >= 1300000 && skillId < 1320000;
            case 132:
                return skillId >= 1000000 && skillId < 1100000 || skillId >= 1300000 && skillId < 1330000;
            case 200:
                return skillId >= 2000000 && skillId < 2100000;
            case 210:
                return skillId >= 2000000 && skillId < 2110000;
            case 211:
                return skillId >= 2000000 && skillId < 2120000;
            case 212:
                return skillId >= 2000000 && skillId < 2130000;
            case 220:
                return skillId >= 2000000 && skillId < 2100000 || skillId >= 2200000 && skillId < 2210000;
            case 221:
                return skillId >= 2000000 && skillId < 2100000 || skillId >= 2200000 && skillId < 2220000;
            case 222:
                return skillId >= 2000000 && skillId < 2100000 || skillId >= 2200000 && skillId < 2230000;
            case 230:
                return skillId >= 2000000 && skillId < 2100000 || skillId >= 2300000 && skillId < 2310000;
            case 231:
                return skillId >= 2000000 && skillId < 2100000 || skillId >= 2300000 && skillId < 2320000;
            case 232:
                return skillId >= 2000000 && skillId < 2100000 || skillId >= 2300000 && skillId < 2330000;
            case 300:
                return skillId >= 3000000 && skillId < 3100000;
            case 310:
                return skillId >= 3000000 && skillId < 3110000;
            case 311:
                return skillId >= 3000000 && skillId < 3120000;
            case 312:
                return skillId >= 3000000 && skillId < 3130000;
            case 320:
                return skillId >= 3000000 && skillId < 3100000 || skillId >= 3200000 && skillId < 3210000;
            case 321:
                return skillId >= 3000000 && skillId < 3100000 || skillId >= 3200000 && skillId < 3220000;
            case 322:
                return skillId >= 3000000 && skillId < 3100000 || skillId >= 3200000 && skillId < 3230000;
            case 400:
                return skillId >= 4000000 && skillId < 4100000;
            case 410:
                return skillId >= 4000000 && skillId < 4110000;
            case 411:
                return skillId >= 4000000 && skillId < 4120000;
            case 412:
                return skillId >= 4000000 && skillId < 4130000;
            case 420:
                return skillId >= 4000000 && skillId < 4110000 || skillId >= 4200000 && skillId < 4210000;
            case 421:
                return skillId >= 4000000 && skillId < 4110000 || skillId >= 4200000 && skillId < 4220000;
            case 422:
                return skillId >= 4000000 && skillId < 4110000 || skillId >= 4200000 && skillId < 4230000;
            case 430:
                return skillId >= 4000000 && skillId < 4110000 || skillId >= 4300000 && skillId < 4310000;
            case 431:
                return skillId >= 4000000 && skillId < 4110000 || skillId >= 4300000 && skillId < 4320000;
            case 432:
                return skillId >= 4000000 && skillId < 4110000 || skillId >= 4300000 && skillId < 4330000;
            case 433:
                return skillId >= 4000000 && skillId < 4110000 || skillId >= 4300000 && skillId < 4340000;
            case 434:
                return skillId >= 4000000 && skillId < 4110000 || skillId >= 4300000 && skillId < 4350000;
            case 500:
                return skillId >= 5000000 && skillId < 5100000;
            case 510:
                return skillId >= 5000000 && skillId < 5110000;
            case 511:
                return skillId >= 5000000 && skillId < 5120000;
            case 512:
                return skillId >= 5000000 && skillId < 5130000;
            case 520:
                return skillId >= 5000000 && skillId < 5100000 || skillId >= 5200000 && skillId < 5210000;
            case 521:
                return skillId >= 5000000 && skillId < 5100000 || skillId >= 5200000 && skillId < 5220000;
            case 522:
                return skillId >= 5000000 && skillId < 5100000 || skillId >= 5200000 && skillId < 5230000;
            case 1100:
                return skillId >= 11000000 && skillId < 11100000;
            case 1110:
                return skillId >= 11000000 && skillId < 11110000;
            case 1111:
                return skillId >= 11000000 && skillId < 11120000;
            case 1112:
                return skillId >= 11000000 && skillId < 11130000;
            case 1200:
                return skillId >= 12000000 && skillId < 12100000;
            case 1210:
                return skillId >= 12000000 && skillId < 12110000;
            case 1211:
                return skillId >= 12000000 && skillId < 12120000;
            case 1212:
                return skillId >= 12000000 && skillId < 12130000;
            case 1300:
                return skillId >= 13000000 && skillId < 13100000;
            case 1310:
                return skillId >= 13000000 && skillId < 13110000;
            case 1311:
                return skillId >= 13000000 && skillId < 13120000;
            case 1312:
                return skillId >= 13000000 && skillId < 13130000;
            case 1400:
                return skillId >= 14000000 && skillId < 14100000;
            case 1410:
                return skillId >= 14000000 && skillId < 14110000;
            case 1411:
                return skillId >= 14000000 && skillId < 14120000;
            case 1412:
                return skillId >= 14000000 && skillId < 14130000;
            case 1500:
                return skillId >= 15000000 && skillId < 15100000;
            case 1510:
                return skillId >= 15000000 && skillId < 15110000;
            case 1511:
                return skillId >= 15000000 && skillId < 15120000;
            case 1512:
                return skillId >= 15000000 && skillId < 15130000;
            case 2100:
                return skillId >= 21000000 && skillId < 21100000;
            case 2110:
                return skillId >= 21000000 && skillId < 21110000;
            case 2111:
                return skillId >= 21000000 && skillId < 21120000;
            case 2112:
                return skillId >= 21000000 && skillId < 21130000;
            case 2200:
                return skillId >= 22000000 && skillId < 22100000;
            case 2210:
                return skillId >= 22000000 && skillId < 22110000;
            case 2211:
                return skillId >= 22000000 && skillId < 22120000;
            case 2212:
                return skillId >= 22000000 && skillId < 22130000;
            case 2213:
                return skillId >= 22000000 && skillId < 22140000;
            case 2214:
                return skillId >= 22000000 && skillId < 22150000;
            case 2215:
                return skillId >= 22000000 && skillId < 22160000;
            case 2216:
                return skillId >= 22000000 && skillId < 22170000;
            case 2217:
                return skillId >= 22000000 && skillId < 22180000;
            case 2218:
                return skillId >= 22000000 && skillId < 22190000;
            default:
                return false;
        }
    }

    public static boolean isCurrentJobSkill(int skillId, int jobId) {
        switch (jobId) {
            case 100:
                return skillId >= 1000000 && skillId < 1100000;
            case 110:
                return skillId >= 1100000 && skillId < 1110000;
            case 111:
                return skillId >= 1110000 && skillId < 1120000;
            case 112:
                return skillId >= 1120000 && skillId < 1130000;
            case 120:
                return skillId >= 1200000 && skillId < 1210000;
            case 121:
                return skillId >= 1210000 && skillId < 1220000;
            case 122:
                return skillId >= 1220000 && skillId < 1230000;
            case 130:
                return skillId >= 1300000 && skillId < 1310000;
            case 131:
                return skillId >= 1310000 && skillId < 1320000;
            case 132:
                return skillId >= 1320000 && skillId < 1330000;
            case 200:
                return skillId >= 2000000 && skillId < 2100000;
            case 210:
                return skillId >= 2100000 && skillId < 2110000;
            case 211:
                return skillId >= 2110000 && skillId < 2120000;
            case 212:
                return skillId >= 2120000 && skillId < 2130000;
            case 220:
                return skillId >= 2200000 && skillId < 2210000;
            case 221:
                return skillId >= 2210000 && skillId < 2220000;
            case 222:
                return skillId >= 2210000 && skillId < 2230000;
            case 230:
                return skillId >= 2300000 && skillId < 2310000;
            case 231:
                return skillId >= 2310000 && skillId < 2320000;
            case 232:
                return skillId >= 2320000 && skillId < 2330000;
            case 300:
                return skillId >= 3000000 && skillId < 3100000;
            case 310:
                return skillId >= 3100000 && skillId < 3110000;
            case 311:
                return skillId >= 3110000 && skillId < 3120000;
            case 312:
                return skillId >= 3120000 && skillId < 3130000;
            case 320:
                return skillId >= 3200000 && skillId < 3210000;
            case 321:
                return skillId >= 3210000 && skillId < 3220000;
            case 322:
                return skillId >= 3220000 && skillId < 3230000;
            case 400:
                return skillId >= 4000000 && skillId < 4100000;
            case 410:
                return skillId >= 4100000 && skillId < 4110000;
            case 411:
                return skillId >= 4110000 && skillId < 4120000;
            case 412:
                return skillId >= 4120000 && skillId < 4130000;
            case 420:
                return skillId >= 4200000 && skillId < 4210000;
            case 421:
                return skillId >= 4210000 && skillId < 4220000;
            case 422:
                return skillId >= 4220000 && skillId < 4230000;
            case 500:
                return skillId >= 5000000 && skillId < 5100000;
            case 510:
                return skillId >= 5100000 && skillId < 5110000;
            case 511:
                return skillId >= 5110000 && skillId < 5120000;
            case 512:
                return skillId >= 5120000 && skillId < 5130000;
            case 520:
                return skillId >= 5200000 && skillId < 5210000;
            case 521:
                return skillId >= 5210000 && skillId < 5220000;
            case 522:
                return skillId >= 5220000 && skillId < 5230000;
            case 1100:
                return skillId >= 11000000 && skillId < 11100000;
            case 1110:
                return skillId >= 11100000 && skillId < 11110000;
            case 1111:
                return skillId >= 11110000 && skillId < 11120000;
            case 1112:
                return skillId >= 11120000 && skillId < 11130000;
            case 1200:
                return skillId >= 12000000 && skillId < 12100000;
            case 1210:
                return skillId >= 12100000 && skillId < 12110000;
            case 1211:
                return skillId >= 12110000 && skillId < 12120000;
            case 1212:
                return skillId >= 12120000 && skillId < 12130000;
            case 1300:
                return skillId >= 13000000 && skillId < 13100000;
            case 1310:
                return skillId >= 13100000 && skillId < 13110000;
            case 1311:
                return skillId >= 13110000 && skillId < 13120000;
            case 1312:
                return skillId >= 13120000 && skillId < 13130000;
            case 1400:
                return skillId >= 14000000 && skillId < 14100000;
            case 1410:
                return skillId >= 14100000 && skillId < 14110000;
            case 1411:
                return skillId >= 14110000 && skillId < 14120000;
            case 1412:
                return skillId >= 14120000 && skillId < 14130000;
            case 1500:
                return skillId >= 15000000 && skillId < 15100000;
            case 1510:
                return skillId >= 15100000 && skillId < 15110000;
            case 1511:
                return skillId >= 15110000 && skillId < 15120000;
            case 1512:
                return skillId >= 15120000 && skillId < 15130000;
            case 2100:
                return skillId >= 21000000 && skillId < 21100000;
            case 2110:
                return skillId >= 21100000 && skillId < 21110000;
            case 2111:
                return skillId >= 21110000 && skillId < 21120000;
            case 2112:
                return skillId >= 21120000 && skillId < 21130000;
            default:
                return false;
        }
    }

    public static int getJobStage(int jobId) {
        switch (jobId) {
            case 0:
            case 1000:
            case 2000:
            case 2001:
                return 0;
            case 100:
            case 200:
            case 300:
            case 400:
            case 500:
            case 1100:
            case 1200:
            case 1300:
            case 1400:
            case 1500:
            case 2100:
            case 2200:
                return 1;
            case 110:
            case 120:
            case 130:
            case 210:
            case 220:
            case 230:
            case 310:
            case 320:
            case 410:
            case 420:
            case 430:
            case 510:
            case 520:
            case 1110:
            case 1210:
            case 1310:
            case 1410:
            case 1510:
            case 2110:
            case 2210:
                return 2;
            case 111:
            case 121:
            case 131:
            case 211:
            case 221:
            case 231:
            case 311:
            case 321:
            case 411:
            case 421:
            case 431:
            case 511:
            case 521:
            case 1111:
            case 1211:
            case 1311:
            case 1411:
            case 1511:
            case 2111:
            case 2211:
                return 3;
            case 112:
            case 122:
            case 132:
            case 212:
            case 222:
            case 232:
            case 312:
            case 322:
            case 412:
            case 422:
            case 432:
            case 512:
            case 522:
            case 1112:
            case 1212:
            case 1312:
            case 1412:
            case 1512:
            case 2112:
            case 2212:
                return 4;
            case 433:
            case 2213:
                return 5;
            case 434:
            case 2214:
                return 6;
            case 2215:
                return 7;
            case 2216:
                return 8;
            case 2217:
                return 9;
            case 2218:
                return 10;
            default:
                return -1;
        }
    }

    public static boolean isJobSkill2(int skillId, int jobId) {
        List<Integer> skills = new ArrayList();
        switch (jobId) {
            case 100:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1001003, 1001004, 1001005);
                break;
            case 110:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1100000, 1100001, 1100002, 1100003, 1101004, 1101005, 1101006, 1101007);
                break;
            case 111:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1100000, 1100001, 1100002, 1100003, 1101004, 1101005, 1101006, 1101007, 1110000, 1110001, 1111002, 1111003, 1111004, 1111005, 1111006, 1111007, 1111008);
                break;
            case 112:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1100000, 1100001, 1100002, 1100003, 1101004, 1101005, 1101006, 1101007, 1110000, 1110001, 1111002, 1111003, 1111004, 1111005, 1111006, 1111007, 1111008, 1120003, 1120004, 1120005, 1121000, 1121001, 1121002, 1121006, 1121008, 1121010, 1121011);
                break;
            case 120:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1200000, 1200001, 1200002, 1200003, 1201004, 1201005, 1201006, 1201007);
                break;
            case 121:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1200000, 1200001, 1200002, 1200003, 1201004, 1201005, 1201006, 1201007, 1210000, 1210001, 1211002, 1211003, 1211004, 1211005, 1211006, 1211007, 1211008, 1211009);
                break;
            case 122:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1200000, 1200001, 1200002, 1200003, 1201004, 1201005, 1201006, 1201007, 1210000, 1210001, 1211002, 1211003, 1211004, 1211005, 1211006, 1211007, 1211008, 1211009, 1220005, 1220006, 1220010, 1221000, 1221001, 1221002, 1221003, 1221004, 1221005, 1221006, 1221007, 1221008, 1221009, 1221010, 1221011, 1221012);
                break;
            case 130:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1300000, 1300001, 1300002, 1300003, 1301004, 1301005, 1301006, 1301007);
                break;
            case 131:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1300000, 1300001, 1300002, 1300003, 1301004, 1301005, 1301006, 1301007, 1310000, 1311001, 1311002, 1311003, 1311004, 1311005, 1311006, 1311007, 1311008);
                break;
            case 132:
                skills = Arrays.asList(1000000, 1000001, 1000002, 1000003, 1000004, 1000005, 1300000, 1300001, 1300002, 1300003, 1301004, 1301005, 1301006, 1301007, 1310000, 1311001, 1311002, 1311003, 1311004, 1311005, 1311006, 1311007, 1311008, 1320005, 1320006, 1320007, 1320008, 1320009, 1321000, 1321001, 1321002, 1321003, 1321007, 1321010);
                break;
            case 200:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005);
                break;
            case 210:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2100000, 2101001, 2101002, 2101003, 2101004, 2101005);
                break;
            case 211:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2100000, 2101001, 2101002, 2101003, 2101004, 2101005, 2110000, 2110001, 2111002, 2111003, 2111004, 2111005, 2111006);
                break;
            case 212:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2100000, 2101001, 2101002, 2101003, 2101004, 2101005, 2110000, 2110001, 2111002, 2111003, 2111004, 2111005, 2111006, 2121000, 2121001, 2121002, 2121003, 2121004, 2121005, 2121006, 2121007, 2121008);
                break;
            case 220:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2200000, 2201001, 2201002, 2201003, 2201004, 2201005);
                break;
            case 221:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2200000, 2201001, 2201002, 2201003, 2201004, 2201005, 2210000, 2210001, 2211002, 2211003, 2211004, 2211005, 2211006);
                break;
            case 222:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2200000, 2201001, 2201002, 2201003, 2201004, 2201005, 2210000, 2210001, 2211002, 2211003, 2211004, 2211005, 2211006, 2221000, 2221001, 2221002, 2221003, 2221004, 2221005, 2221006, 2221007, 2221008);
                break;
            case 230:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2300000, 2301001, 2301002, 2301003, 2301004, 2301005);
                break;
            case 231:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2300000, 2301001, 2301002, 2301003, 2301004, 2301005, 2310000, 2311001, 2311002, 2311003, 2311004, 2311005, 2311006);
                break;
            case 232:
                skills = Arrays.asList(2000000, 2000001, 2001002, 2001003, 2001004, 2001005, 2300000, 2301001, 2301002, 2301003, 2301004, 2301005, 2310000, 2311001, 2311002, 2311003, 2311004, 2311005, 2311006, 2321000, 2321001, 2321002, 2321003, 2321004, 2321005, 2321006, 2321007, 2321008, 2321009);
                break;
            case 300:
                skills = Arrays.asList(3000000, 3000001, 3000002, 3001003, 3001004, 3001005);
                break;
            case 310:
                skills = Arrays.asList(3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 3100000, 3100001, 3101002, 3101003, 3101004, 3101005);
                break;
            case 311:
                skills = Arrays.asList(3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 3100000, 3100001, 3101002, 3101003, 3101004, 3101005, 3110000, 3110001, 3111002, 3111003, 3111004, 3111005, 3111006);
                break;
            case 312:
                skills = Arrays.asList(3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 3100000, 3100001, 3101002, 3101003, 3101004, 3101005, 3110000, 3110001, 3111002, 3111003, 3111004, 3111005, 3111006, 3120005, 3121000, 3121002, 3121003, 3121004, 3121005, 3121006, 3121007, 3121008, 3121009);
                break;
            case 320:
                skills = Arrays.asList(3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 3200000, 3200001, 3201002, 3201003, 3201004, 3201005);
                break;
            case 321:
                skills = Arrays.asList(3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 3200000, 3200001, 3201002, 3201003, 3201004, 3201005, 3210000, 3210001, 3211002, 3211003, 3211004, 3211005, 3211006);
                break;
            case 322:
                skills = Arrays.asList(3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 3200000, 3200001, 3201002, 3201003, 3201004, 3201005, 3210000, 3210001, 3211002, 3211003, 3211004, 3211005, 3211006, 3220004, 3221000, 3221001, 3221002, 3221003, 3221004, 3221005, 3221006, 3221007, 3221008);
                break;
            case 400:
                skills = Arrays.asList(4000000, 4000001, 4001002, 4001003, 4001334, 4001344);
                break;
            case 410:
                skills = Arrays.asList(4000000, 4000001, 4001002, 4001003, 4001334, 4001344, 4100000, 4100001, 4100002, 4101003, 4101004, 4101005);
                break;
            case 411:
                skills = Arrays.asList(4000000, 4000001, 4001002, 4001003, 4001334, 4001344, 4100000, 4100001, 4100002, 4101003, 4101004, 4101005, 4110000, 4111001, 4111002, 4111003, 4111004, 4111005, 4111006);
                break;
            case 412:
                skills = Arrays.asList(4000000, 4000001, 4001002, 4001003, 4001334, 4001344, 4100000, 4100001, 4100002, 4101003, 4101004, 4101005, 4110000, 4111001, 4111002, 4111003, 4111004, 4111005, 4111006, 4120002, 4120005, 4121000, 4121003, 4121004, 4121005, 4121006, 4121007, 4121008, 4121009);
                break;
            case 420:
                skills = Arrays.asList(4000000, 4000001, 4001002, 4001003, 4001334, 4001344, 4200000, 4200001, 4201002, 4201003, 4201004, 4201005);
                break;
            case 421:
                skills = Arrays.asList(4000000, 4000001, 4001002, 4001003, 4001334, 4001344, 4200000, 4200001, 4201002, 4201003, 4201004, 4201005, 4210000, 4211001, 4211002, 4211003, 4211004, 4211005, 4211006);
                break;
            case 422:
                skills = Arrays.asList(4000000, 4000001, 4001002, 4001003, 4001334, 4001344, 4200000, 4200001, 4201002, 4201003, 4201004, 4201005, 4210000, 4211001, 4211002, 4211003, 4211004, 4211005, 4211006, 4220002, 4220005, 4221000, 4221001, 4221002, 4221003, 4221004, 4221005, 4221006, 4221007, 4221008);
                break;
            case 500:
                skills = Arrays.asList(5000000, 5001001, 5001002, 5001003, 5001004, 5001005);
                break;
            case 510:
                skills = Arrays.asList(5000000, 5001001, 5001002, 5001003, 5001004, 5001005, 5100000, 5100001, 5101002, 5101003, 5101004, 5101005, 5101006, 5101007);
                break;
            case 511:
                skills = Arrays.asList(5000000, 5001001, 5001002, 5001003, 5001004, 5001005, 5100000, 5100001, 5101002, 5101003, 5101004, 5101005, 5101006, 5101007, 5110000, 5110001, 5111002, 5111004, 5111005, 5111006);
                break;
            case 512:
                skills = Arrays.asList(5000000, 5001001, 5001002, 5001003, 5001004, 5001005, 5100000, 5100001, 5101002, 5101003, 5101004, 5101005, 5101006, 5101007, 5110000, 5110001, 5111002, 5111004, 5111005, 5111006, 5121000, 5121001, 5121002, 5121003, 5121004, 5121005, 5121006, 5121007, 5121008, 5121009, 5121010);
                break;
            case 520:
                skills = Arrays.asList(5000000, 5001001, 5001002, 5001003, 5001004, 5001005, 5200000, 5201001, 5201002, 5201003, 5201004, 5201005, 5201006);
                break;
            case 521:
                skills = Arrays.asList(5000000, 5001001, 5001002, 5001003, 5001004, 5001005, 5200000, 5201001, 5201002, 5201003, 5201004, 5201005, 5201006, 5210000, 5211001, 5211002, 5211003, 5211004, 5211005, 5211006);
                break;
            case 522:
                skills = Arrays.asList(5000000, 5001001, 5001002, 5001003, 5001004, 5001005, 5200000, 5201001, 5201002, 5201003, 5201004, 5201005, 5201006, 5210000, 5211001, 5211002, 5211003, 5211004, 5211005, 5211006, 5220001, 5220002, 5220011, 5221000, 5221003, 5221004, 5221005, 5221006, 5221007, 5221008, 5221009, 5221010);
                break;
            case 1100:
                skills = Arrays.asList(11000000, 11001001, 11001002, 11001003, 11001004);
                break;
            case 1110:
                skills = Arrays.asList(11000000, 11001001, 11001002, 11001003, 11001004, 11100000, 11101001, 11101002, 11101003, 11101004, 11101005);
                break;
            case 1111:
                skills = Arrays.asList(11000000, 11001001, 11001002, 11001003, 11001004, 11100000, 11101001, 11101002, 11101003, 11101004, 11101005, 11110000, 11110005, 11111001, 11111002, 11111003, 11111004, 11111005, 11111006, 11111007);
                break;
            case 1200:
                skills = Arrays.asList(12000000, 12001001, 12001002, 12001003, 12001004);
                break;
            case 1210:
                skills = Arrays.asList(12000000, 12001001, 12001002, 12001003, 12001004, 12101000, 12101001, 12101002, 12101003, 12101004, 12101005, 12101006);
                break;
            case 1211:
                skills = Arrays.asList(12000000, 12001001, 12001002, 12001003, 12001004, 12101000, 12101001, 12101002, 12101003, 12101004, 12101005, 12101006, 12110000, 12110001, 12111002, 12111003, 12111004, 12111005, 12111006);
                break;
            case 1300:
                skills = Arrays.asList(13000000, 13000001, 13001002, 13001003, 13001004);
                break;
            case 1310:
                skills = Arrays.asList(13000000, 13000001, 13001002, 13001003, 13001004, 13100000, 13100004, 13101001, 13101002, 13101003, 13101004, 13101005, 13101006);
                break;
            case 1311:
                skills = Arrays.asList(13000000, 13000001, 13001002, 13001003, 13001004, 13100000, 13100004, 13101001, 13101002, 13101003, 13101004, 13101005, 13101006, 13110003, 13111000, 13111001, 13111002, 13111003, 13111004, 13111005, 13111006, 13111007);
                break;
            case 1400:
                skills = Arrays.asList(14000000, 14000001, 14001002, 14001003, 14001004, 14001005);
                break;
            case 1410:
                skills = Arrays.asList(14000000, 14000001, 14001002, 14001003, 14001004, 14001005, 14100000, 14100001, 14100005, 14101002, 14101003, 14101004, 14101005, 14101006);
                break;
            case 1411:
                skills = Arrays.asList(14000000, 14000001, 14001002, 14001003, 14001004, 14001005, 14100000, 14100001, 14100005, 14101002, 14101003, 14101004, 14101005, 14101006, 14110003, 14110004, 14111000, 14111001, 14111002, 14111003, 14111004, 14111005, 14111006);
                break;
            case 1500:
                skills = Arrays.asList(15000000, 15001001, 15001002, 15001003, 15001004);
                break;
            case 1510:
                skills = Arrays.asList(15000000, 15001001, 15001002, 15001003, 15001004, 15100000, 15100001, 15100004, 15101002, 15101003, 15101004, 15101005, 15101006);
                break;
            case 1511:
                skills = Arrays.asList(15000000, 15001001, 15001002, 15001003, 15001004, 15100000, 15100001, 15100004, 15101002, 15101003, 15101004, 15101005, 15101006, 15110000, 15111001, 15111002, 15111003, 15111004, 15111005, 15111006, 15111007);
                break;
            case 2100:
                skills = Arrays.asList(21000000, 21000002, 21001001, 21001003);
                break;
            case 2110:
                skills = Arrays.asList(21000000, 21000002, 21001001, 21001003, 21100000, 21100001, 21100002, 21100003, 21100004, 21100005);
                break;
            case 2111:
                skills = Arrays.asList(21000000, 21000002, 21001001, 21001003, 21100000, 21100001, 21100002, 21100003, 21100004, 21100005, 21110000, 21110001, 21110002, 21110003, 21110004, 21110005, 21110006, 21110007, 21110008);
                break;
            case 2112:
                skills = Arrays.asList(21000000, 21000002, 21001001, 21001003, 21100000, 21100001, 21100002, 21100003, 21100004, 21100005, 21110000, 21110001, 21110002, 21110003, 21110004, 21110005, 21110006, 21110007, 21110008, 21121000, 21121001, 21121002, 21121003, 21121004, 21121005, 21121006, 21121007, 21121008, 21121009, 21121010);
        }

        return ((List)skills).contains(skillId);
    }

    public static int getJobStageBySkillId(int skillId) {
        if (skillId < 10000000) {
            if ((int)((double)skillId / 1000000.0) == 0) {
                return 0;
            } else if ((int)((double)skillId / 100000.0) % 10 == 0) {
                return 1;
            } else if ((int)((double)skillId / 10000.0) % 10 == 0) {
                return 2;
            } else if ((int)((double)skillId / 10000.0) % 10 == 1) {
                return 3;
            } else {
                return (int)((double)skillId / 10000.0) % 10 == 2 ? 4 : -1;
            }
        } else {
            return -1;
        }
    }

    public static int getInitialMasterLevel(int skillId) {
        for(int i = 0; i < initialMasterLevelList.length; ++i) {
            if (initialMasterLevelList[i][0] == skillId) {
                return initialMasterLevelList[i][1];
            }
        }

        return 0;
    }

    public static String getSkillName(int skillId) {
        String name = "";
        new HashMap();
        Map<Integer, String> skillMap = SearchGenerator.getSearchData(SearchType.技能, skillId + "");
        name = (String)skillMap.get(skillId);
        return name;
    }

    public static boolean isMagicSkill(int skillId) {
        if (skillId >= 1000000 && skillId < 10000000) {
            if (skillId >= 2000000 && skillId < 3000000) {
                return true;
            }
        } else if (skillId > 10000000) {
            if (skillId >= 12000000 && skillId < 13000000) {
                return true;
            }

            if (skillId >= 22000000 && skillId < 30000000) {
                return true;
            }

            if (skillId >= 32000000 && skillId < 33000000) {
                return true;
            }
        }

        return false;
    }
}
