package j.tool.region;

import j.util.ConcurrentList;
import j.util.JUtilPinYin;
import j.util.JUtilSorter;
import j.util.JUtilString;

import java.util.ArrayList;
import java.util.List;

public class Countries {
	private static List<CountryData> countries=new ArrayList<>();
	private static List<CountryData> iso=new ArrayList<>();
	public static final String DEFAULT_MOBILE_CODE="86";
	public static final String DEFAULT_COUNTRY_CODE="CN";

	static {
		countries.add(new CountryData("AF", "AFG", "004", "93", "", "阿富汗", "Afghanistan", "the Islamic Republic of Afghanistan", "", "^(\\+|(00))?((93\\-)|(93))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AX", "ALA", "248", "35818", "", "奥兰群岛", "Aland Islands", "Aland Islands", "", "^(\\+|(00))?((35818\\-)|(35818))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AL", "ALB", "008", "355", "", "阿尔巴尼亚", "Albania", "the Republic of Albania", "", "^(\\+|(00))?((355\\-)|(355))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("DZ", "DZA", "012", "213", "", "阿尔及利亚", "Algeria", "the People's Democratic Republic of Algeria", "", "^(\\+|(00))?((213\\-)|(213))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AS", "ASM", "016", "1", "", "美属萨摩亚", "American Samoa", "American Samoa", "", "^(\\+|(00))?((1\\-)|(1))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AD", "AND", "020", "376", "", "安道尔", "Andorra", "the Principality of Andorra", "", "^(\\+|(00))?((376\\-)|(376))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AO", "AGO", "024", "244", "", "安哥拉", "Angola", "the Republic of Angola", "", "^(\\+|(00))?((244\\-)|(244))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AI", "AIA", "660", "1264", "", "安圭拉", "Anguilla", "Anguilla", "", "^(\\+|(00))?((1264\\-)|(1264))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AQ", "ATA", "010", "64672", "", "南极洲", "Antarctica", "Antarctica", "", "^(\\+|(00))?((64672\\-)|(64672))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AG", "ATG", "028", "1268", "", "安提瓜和巴布达", "Antigua and Barbuda", "Antigua and Barbuda", "", "^(\\+|(00))?((1268\\-)|(1268))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AR", "ARG", "032", "54", "", "阿根廷", "Argentina", "the Argentine Republic", "", "^(\\+|(00))?((54\\-)|(54))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AM", "ARM", "051", "374", "", "亚美尼亚", "Armenia", "the Republic of Armenia", "", "^(\\+|(00))?((374\\-)|(374))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AW", "ABW", "533", "297", "", "阿鲁巴", "Aruba", "Aruba", "", "^(\\+|(00))?((297\\-)|(297))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AU", "AUS", "036", "61", "", "澳大利亚", "Australia", "Australia", "", "^(\\+|(00))?((61\\-)|(61))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AT", "AUT", "040", "43", "", "奥地利", "Austria", "the Republic of Austria", "", "^(\\+|(00))?((43\\-)|(43))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AZ", "AZE", "031", "994", "", "阿塞拜疆", "Azerbaijan", "the Republic of Azerbaijan", "", "^(\\+|(00))?((994\\-)|(994))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BS", "BHS", "044", "1242", "", "巴哈马", "Bahamas (The)", "the Commonwealth of The Bahamas", "", "^(\\+|(00))?((1242\\-)|(1242))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BH", "BHR", "048", "973", "", "巴林", "Bahrain", "the Kingdom of Bahrain", "", "^(\\+|(00))?((973\\-)|(973))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BD", "BGD", "050", "880", "", "孟加拉国", "Bangladesh", "the People's Republic of Bangladesh", "", "^(\\+|(00))?((880\\-)|(880))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BB", "BRB", "052", "1246", "", "巴巴多斯", "Barbados", "Barbados", "", "^(\\+|(00))?((1246\\-)|(1246))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BY", "BLR", "112", "375", "", "白俄罗斯", "Belarus", "the Republic of Belarus", "", "^(\\+|(00))?((375\\-)|(375))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BE", "BEL", "056", "32", "", "比利时", "Belgium", "the Kingdom of Belgium", "", "^(\\+|(00))?((32\\-)|(32))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BZ", "BLZ", "084", "501", "", "伯利兹", "Belize", "Belize", "", "^(\\+|(00))?((501\\-)|(501))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BJ", "BEN", "204", "229", "", "贝宁", "Benin", "the Republic of Benin", "", "^(\\+|(00))?((229\\-)|(229))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BM", "BMU", "060", "1441", "", "百慕大", "Bermuda", "Bermuda", "", "^(\\+|(00))?((1441\\-)|(1441))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BT", "BTN", "064", "975", "", "不丹", "Bhutan", "the Kingdom of Bhutan", "", "^(\\+|(00))?((975\\-)|(975))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BO", "BOL", "068", "591", "", "玻利维亚", "Bolivia", "the Republic of Bolivia", "", "^(\\+|(00))?((591\\-)|(591))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BA", "BIH", "070", "387", "", "波黑", "Bosnia and Herzegovina", "Bosnia and Herzegovina", "", "^(\\+|(00))?((387\\-)|(387))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BW", "BWA", "072", "267", "", "博茨瓦纳", "Botswana", "the Republic of Botswana", "", "^(\\+|(00))?((267\\-)|(267))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BV", "BVT", "074", "47", "", "布维岛", "Bouvet Island", "Bouvet Island", "", "^(\\+|(00))?((47\\-)|(47))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BR", "BRA", "076", "55", "", "巴西", "Brazil", "the Federative Republic of Brazil", "", "^(\\+|(00))?((55\\-)|(55))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IO", "IOT", "086", "44", "", "英属印度洋领地", "British Indian Ocean Territory (the)", "British Indian Ocean Territory (the)", "", "^(\\+|(00))?((44\\-)|(44))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BN", "BRN", "096", "673", "", "文莱", "Brunei Darussalam", "Brunei Darussalam", "", "^(\\+|(00))?((673\\-)|(673))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BG", "BGR", "100", "359", "", "保加利亚", "Bulgaria", "the Republic of Bulgaria", "", "^(\\+|(00))?((359\\-)|(359))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BF", "BFA", "854", "226", "", "布基纳法索", "Burkina Faso", "Burkina Faso", "", "^(\\+|(00))?((226\\-)|(226))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("BI", "BDI", "108", "257", "", "布隆迪", "Burundi", "the Republic of Burundi", "", "^(\\+|(00))?((257\\-)|(257))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KH", "KHM", "116", "855", "", "柬埔寨", "Cambodia", "the Kingdom of Cambodia", "", "^(\\+|(00))?((855\\-)|(855))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CM", "CMR", "120", "237", "", "喀麦隆", "Cameroon", "the Republic of Cameroon", "", "^(\\+|(00))?((237\\-)|(237))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CA", "CAN", "124", "1", "", "加拿大", "Canada", "Canada", "", "^(\\+|(00))?((1\\-)|(1))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CV", "CPV", "132", "238", "", "佛得角", "Cape Verde", "the Republic of Cape Verde", "", "^(\\+|(00))?((238\\-)|(238))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KY", "CYM", "136", "1345", "", "开曼群岛", "Cayman Islands (the)", "Cayman Islands (the)", "", "^(\\+|(00))?((1345\\-)|(1345))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CF", "CAF", "140", "236", "", "中非", "Central African Republic (the)", "the Central African Republic", "", "^(\\+|(00))?((236\\-)|(236))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TD", "TCD", "148", "235", "", "乍得", "Chad", "the Republic of Chad", "", "^(\\+|(00))?((235\\-)|(235))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CL", "CHL", "152", "56", "", "智利", "Chile", "the Republic of Chile", "", "^(\\+|(00))?((56\\-)|(56))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CN", "CHN", "156", "86", "", "中国", "China", "the People's Republic of China", "", "^(\\+|(00))?((86\\-)|(86))\\d[\\d\\-]+\\d$", "^\\d{11}$", "^\\d{1,4}$"));
		countries.add(new CountryData("CX", "CXR", "162", "618", "", "圣诞岛", "Christmas Island", "Christmas Island", "", "^(\\+|(00))?((618\\-)|(618))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CC", "CCK", "166", "61891", "", "科科斯（基林）群岛", "Cocos (Keeling) Islands (the)", "Cocos (Keeling) Islands (the)", "", "^(\\+|(00))?((61891\\-)|(61891))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CO", "COL", "170", "57", "", "哥伦比亚", "Colombia", "the Republic of Colombia", "", "^(\\+|(00))?((57\\-)|(57))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KM", "COM", "174", "269", "", "科摩罗", "Comoros", "the Union of the Comoros", "", "^(\\+|(00))?((269\\-)|(269))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CG", "COG", "178", "242", "", "刚果（布）", "Congo", "the Republic of the Congo", "", "^(\\+|(00))?((242\\-)|(242))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CD", "COD", "180", "243", "", "刚果（金）", "Congo (the Democratic Republic of the)", "the Democratic Republic of the Congo", "", "^(\\+|(00))?((243\\-)|(243))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CK", "COK", "184", "682", "", "库克群岛", "Cook Islands (the)", "Cook Islands (the)", "", "^(\\+|(00))?((682\\-)|(682))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CR", "CRI", "188", "506", "", "哥斯达黎加", "Costa Rica", "the Republic of Costa Rica", "", "^(\\+|(00))?((506\\-)|(506))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CI", "CIV", "384", "225", "", "科特迪瓦", "C?te d'Ivoire", "the Republic of C?te d'Ivoire", "", "^(\\+|(00))?((225\\-)|(225))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("HR", "HRV", "191", "385", "", "克罗地亚", "Croatia", "the Republic of Croatia", "", "^(\\+|(00))?((385\\-)|(385))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CU", "CUB", "192", "53", "", "古巴", "Cuba", "the Republic of Cuba", "", "^(\\+|(00))?((53\\-)|(53))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CY", "CYP", "196", "357", "", "塞浦路斯", "Cyprus", "the Republic of Cyprus", "", "^(\\+|(00))?((357\\-)|(357))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CZ", "CZE", "203", "420", "", "捷克", "Czech Republic (the)", "the Czech Republic", "", "^(\\+|(00))?((420\\-)|(420))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("DK", "DNK", "208", "45", "", "丹麦", "Denmark", "the Kingdom of Denmark", "", "^(\\+|(00))?((45\\-)|(45))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("DJ", "DJI", "262", "253", "", "吉布提", "Djibouti", "the Republic of Djibouti", "", "^(\\+|(00))?((253\\-)|(253))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("DM", "DMA", "212", "1767", "", "多米尼克", "Dominica", "the Commonwealth of Dominica", "", "^(\\+|(00))?((1767\\-)|(1767))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("DO", "DOM", "214", "18", "", "多米尼加", "Dominican Republic (the)", "the Dominican Republic", "", "^(\\+|(00))?((18\\-)|(18))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("EC", "ECU", "218", "593", "", "厄瓜多尔", "Ecuador", "the Republic of Ecuador", "", "^(\\+|(00))?((593\\-)|(593))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("EG", "EGY", "818", "20", "", "埃及", "Egypt", "the Arab Republic of Egypt", "", "^(\\+|(00))?((20\\-)|(20))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SV", "SLV", "222", "503", "", "萨尔瓦多", "El Salvador", "the Republic of El Salvador", "", "^(\\+|(00))?((503\\-)|(503))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GQ", "GNQ", "226", "240", "", "赤道几内亚", "Equatorial Guinea", "the Republic of Equatorial Guinea", "", "^(\\+|(00))?((240\\-)|(240))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ER", "ERI", "232", "291", "", "厄立特里亚", "Eritrea", "Eritrea", "", "^(\\+|(00))?((291\\-)|(291))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("EE", "EST", "233", "372", "", "爱沙尼亚", "Estonia", "the Republic of Estonia", "", "^(\\+|(00))?((372\\-)|(372))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ET", "ETH", "231", "251", "", "埃塞俄比亚", "Ethiopia", "the Federal Democratic Republic of Ethiopia", "", "^(\\+|(00))?((251\\-)|(251))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("FK", "FLK", "238", "500", "", "福克兰群岛（马尔维纳斯）", "Falkland Islands (the) [Malvinas]", "Falkland Islands (the) [Malvinas]", "", "^(\\+|(00))?((500\\-)|(500))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("FO", "FRO", "234", "298", "", "法罗群岛", "Faroe Islands (the)", "Faroe Islands (the)", "", "^(\\+|(00))?((298\\-)|(298))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("FJ", "FJI", "242", "679", "", "斐济", "Fiji", "the Republic of the Fiji Islands", "", "^(\\+|(00))?((679\\-)|(679))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("FI", "FIN", "246", "358", "", "芬兰", "Finland", "the Republic of Finland", "", "^(\\+|(00))?((358\\-)|(358))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("FR", "FRA", "250", "33", "", "法国", "France", "the French Republic", "", "^(\\+|(00))?((33\\-)|(33))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GF", "GUF", "254", "594", "", "法属圭亚那", "French Guiana", "French Guiana", "", "^(\\+|(00))?((594\\-)|(594))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PF", "PYF", "258", "689", "", "法属波利尼西亚", "French Polynesia", "French Polynesia", "", "^(\\+|(00))?((689\\-)|(689))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TF", "ATF", "260", "33", "", "法属南部领地", "French Southern Territories (the)", "French Southern Territories (the)", "", "^(\\+|(00))?((33\\-)|(33))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GA", "GAB", "266", "241", "", "加蓬", "Gabon", "the Gabonese Republic", "", "^(\\+|(00))?((241\\-)|(241))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GM", "GMB", "270", "220", "", "冈比亚", "Gambia (The)", "the Republic of The Gambia", "", "^(\\+|(00))?((220\\-)|(220))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GE", "GEO", "268", "995", "", "格鲁吉亚", "Georgia", "Georgia", "", "^(\\+|(00))?((995\\-)|(995))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("DE", "DEU", "276", "49", "", "德国", "Germany", "he Federal Republic of Germany", "", "^(\\+|(00))?((49\\-)|(49))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GH", "GHA", "288", "233", "", "加纳", "Ghana", "the Republic of Ghana", "", "^(\\+|(00))?((233\\-)|(233))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GI", "GIB", "292", "350", "", "直布罗陀", "Gibraltar", "Gibraltar", "", "^(\\+|(00))?((350\\-)|(350))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GR", "GRC", "300", "30", "", "希腊", "Greece", "the Hellenic Republic", "", "^(\\+|(00))?((30\\-)|(30))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GL", "GRL", "304", "299", "", "格陵兰", "Greenland", "Greenland", "", "^(\\+|(00))?((299\\-)|(299))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GD", "GRD", "308", "1473", "", "格林纳达", "Grenada", "Grenada", "", "^(\\+|(00))?((1473\\-)|(1473))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GP", "GLP", "312", "590", "", "瓜德罗普", "Guadeloupe", "Guadeloupe", "", "^(\\+|(00))?((590\\-)|(590))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GU", "GUM", "316", "1", "", "关岛", "Guam", "Guam", "", "^(\\+|(00))?((1\\-)|(1))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GT", "GTM", "320", "502", "", "危地马拉", "Guatemala", "the Republic of Guatemala", "", "^(\\+|(00))?((502\\-)|(502))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GG", "GGY", "831", "44", "", "格恩西岛", "Guernsey", "Guernsey", "", "^(\\+|(00))?((44\\-)|(44))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GN", "GIN", "324", "224", "", "几内亚", "Guinea", "the Republic of Guinea", "", "^(\\+|(00))?((224\\-)|(224))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GW", "GNB", "624", "245", "", "几内亚比绍", "Guinea-Bissau", "the Republic of Guinea-Bissau", "", "^(\\+|(00))?((245\\-)|(245))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GY", "GUY", "328", "592", "", "圭亚那", "Guyana", "the Republic of Guyana", "", "^(\\+|(00))?((592\\-)|(592))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("HT", "HTI", "332", "509", "", "海地", "Haiti", "the Republic of Haiti", "", "^(\\+|(00))?((509\\-)|(509))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("HM", "HMD", "334", "61", "", "赫德岛和麦克唐纳岛", "Heard Island and McDonald Islands", "Heard Island and McDonald Islands", "", "^(\\+|(00))?((61\\-)|(61))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("VA", "VAT", "336", "39066", "", "梵蒂冈", "Holy See (the) [Vatican City State]", "Holy See (the) [Vatican City State]", "", "^(\\+|(00))?((39066\\-)|(39066))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("HN", "HND", "340", "504", "", "洪都拉斯", "Honduras", "the Republic of Honduras", "", "^(\\+|(00))?((504\\-)|(504))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("HK", "HKG", "344", "852", "", "中国香港", "Hong Kong", "the Hong Kong Special Administrative Region of China", "", "^(\\+|(00))?((852\\-)|(852))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("HU", "HUN", "348", "36", "", "匈牙利", "Hungary", "the Republic of Hungary", "", "^(\\+|(00))?((36\\-)|(36))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IS", "ISL", "352", "354", "", "冰岛", "Iceland", "the Republic of Iceland", "", "^(\\+|(00))?((354\\-)|(354))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IN", "IND", "356", "91", "", "印度", "India", "the Republic of India", "", "^(\\+|(00))?((91\\-)|(91))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ID", "IDN", "360", "62", "", "印度尼西亚", "Indonesia", "the Republic of Indonesia", "", "^(\\+|(00))?((62\\-)|(62))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IR", "IRN", "364", "98", "", "伊朗", "Iran (the Islamic Republic of)", "the Islamic Republic of Iran", "", "^(\\+|(00))?((98\\-)|(98))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IQ", "IRQ", "368", "964", "", "伊拉克", "Iraq", "the Republic of Iraq", "", "^(\\+|(00))?((964\\-)|(964))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IE", "IRL", "372", "353", "", "爱尔兰", "Ireland", "Ireland", "", "^(\\+|(00))?((353\\-)|(353))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IM", "IMN", "833", "44", "", "英国属地曼岛", "Isle of Man", "Isle of Man", "", "^(\\+|(00))?((44\\-)|(44))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IL", "ISR", "376", "972", "", "以色列", "Israel", "the State of Israel", "", "^(\\+|(00))?((972\\-)|(972))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("IT", "ITA", "380", "39", "", "意大利", "Italy", "the Republic of Italy", "", "^(\\+|(00))?((39\\-)|(39))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("JM", "JAM", "388", "1876", "", "牙买加", "Jamaica", "Jamaica", "", "^(\\+|(00))?((1876\\-)|(1876))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("JP", "JPN", "392", "81", "", "日本", "Japan", "Japan", "", "^(\\+|(00))?((81\\-)|(81))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("JE", "JEY", "832", "44", "", "泽西岛", "Jersey", "Jersey", "", "^(\\+|(00))?((44\\-)|(44))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("JO", "JOR", "400", "962", "", "约旦", "Jordan", "the Hashemite Kingdom of Jordan", "", "^(\\+|(00))?((962\\-)|(962))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KZ", "KAZ", "398", "77", "", "哈萨克斯坦", "Kazakhstan", "the Republic of Kazakhstan", "", "^(\\+|(00))?((77\\-)|(77))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KE", "KEN", "404", "254", "", "肯尼亚", "Kenya", "the Republic of Kenya", "", "^(\\+|(00))?((254\\-)|(254))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KI", "KIR", "296", "686", "", "基里巴斯", "Kiribati", "the Republic of Kiribati", "", "^(\\+|(00))?((686\\-)|(686))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KP", "PRK", "408", "850", "", "朝鲜", "Korea (the Democratic People's Republic of)", "the Democratic People's Republic of Korea", "", "^(\\+|(00))?((850\\-)|(850))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KR", "KOR", "410", "82", "", "韩国", "Korea (the Republic of)", "the Republic of Korea", "", "^(\\+|(00))?((82\\-)|(82))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KW", "KWT", "414", "965", "", "科威特", "Kuwait", "he State of Kuwait", "", "^(\\+|(00))?((965\\-)|(965))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KG", "KGZ", "417", "996", "", "吉尔吉斯斯坦", "Kyrgyzstan", "the Kyrgyz Republic", "", "^(\\+|(00))?((996\\-)|(996))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LA", "LAO", "418", "856", "", "老挝", "Lao People's Democratic Republic (the)", "the Lao People's Democratic Republic", "", "^(\\+|(00))?((856\\-)|(856))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LV", "LVA", "428", "371", "", "拉脱维亚", "Latvia", "the Republic of Latvia", "", "^(\\+|(00))?((371\\-)|(371))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LB", "LBN", "422", "961", "", "黎巴嫩", "Lebanon", "the Lebanese Republic", "", "^(\\+|(00))?((961\\-)|(961))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LS", "LSO", "426", "266", "", "莱索托", "Lesotho", "the Kingdom of Lesotho", "", "^(\\+|(00))?((266\\-)|(266))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LR", "LBR", "430", "231", "", "利比里亚", "Liberia", "the Republic of Liberia", "", "^(\\+|(00))?((231\\-)|(231))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LY", "LBY", "434", "218", "", "利比亚", "Libyan Arab Jamahiriya (the)", "the Socialist People's Libyan Arab Jamahiriya", "", "^(\\+|(00))?((218\\-)|(218))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LI", "LIE", "438", "423", "", "列支敦士登", "Liechtenstein", "the Principality of Liechtenstein", "", "^(\\+|(00))?((423\\-)|(423))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LT", "LTU", "440", "370", "", "立陶宛", "Lithuania", "the Republic of Lithuania", "", "^(\\+|(00))?((370\\-)|(370))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LU", "LUX", "442", "352", "", "卢森堡", "Luxembourg", "the Grand Duchy of Luxembourg", "", "^(\\+|(00))?((352\\-)|(352))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MO", "MAC", "446", "853", "", "中国澳门", "Macao", "Macao Special Administrative Region of China", "", "^(\\+|(00))?((853\\-)|(853))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MK", "MKD", "807", "389", "", "前南马其顿", "Macedonia (the former Yugoslav Republic of)", "the former Yugoslav Republic of Macedonia", "", "^(\\+|(00))?((389\\-)|(389))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MG", "MDG", "450", "261", "", "马达加斯加", "Madagascar", "the Republic of Madagascar", "", "^(\\+|(00))?((261\\-)|(261))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MW", "MWI", "454", "265", "", "马拉维", "Malawi", "the Republic of Malawi", "", "^(\\+|(00))?((265\\-)|(265))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MY", "MYS", "458", "60", "", "马来西亚", "Malaysia", "Malaysia", "", "^(\\+|(00))?((60\\-)|(60))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MV", "MDV", "462", "960", "", "马尔代夫", "Maldives", "the Republic of Maldives", "", "^(\\+|(00))?((960\\-)|(960))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ML", "MLI", "466", "223", "", "马里", "Mali", "the Republic of Mali", "", "^(\\+|(00))?((223\\-)|(223))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MT", "MLT", "470", "356", "", "马耳他", "Malta", "the Republic of Malta", "", "^(\\+|(00))?((356\\-)|(356))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MH", "MHL", "584", "692", "", "马绍尔群岛", "Marshall Islands (the)", "the Republic of the Marshall Islands", "", "^(\\+|(00))?((692\\-)|(692))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MQ", "MTQ", "474", "596", "", "马提尼克", "Martinique", "Martinique", "", "^(\\+|(00))?((596\\-)|(596))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MR", "MRT", "478", "222", "", "毛里塔尼亚", "Mauritania", "the Islamic Republic of Mauritania", "", "^(\\+|(00))?((222\\-)|(222))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MU", "MUS", "480", "230", "", "毛里求斯", "Mauritius", "the Republic of Mauritius", "", "^(\\+|(00))?((230\\-)|(230))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("YT", "MYT", "175", "262", "", "马约特", "Mayotte", "Mayotte", "", "^(\\+|(00))?((262\\-)|(262))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MX", "MEX", "484", "52", "", "墨西哥", "Mexico", "the United Mexican States", "", "^(\\+|(00))?((52\\-)|(52))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("FM", "FSM", "583", "691", "", "密克罗尼西亚联邦", "Micronesia (the Federated States of)", "the Federated States of Micronesia", "", "^(\\+|(00))?((691\\-)|(691))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MD", "MDA", "498", "373", "", "摩尔多瓦", "Moldova (the Republic of)", "the Republic of Moldova", "", "^(\\+|(00))?((373\\-)|(373))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MC", "MCO", "492", "377", "", "摩纳哥", "Monaco", "the Principality of Monaco", "", "^(\\+|(00))?((377\\-)|(377))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MN", "MNG", "496", "976", "", "蒙古", "Mongolia", "Mongolia", "", "^(\\+|(00))?((976\\-)|(976))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ME", "MNE", "499", "382", "", "黑山", "Montenegro", "he Republic of Montenegro", "", "^(\\+|(00))?((382\\-)|(382))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MS", "MSR", "500", "1664", "", "蒙特塞拉特", "Montserrat", "Montserrat", "", "^(\\+|(00))?((1664\\-)|(1664))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MA", "MAR", "504", "212", "", "摩洛哥", "Morocco", "the Kingdom of Morocco", "", "^(\\+|(00))?((212\\-)|(212))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MZ", "MOZ", "508", "258", "", "莫桑比克", "Mozambique", "the Republic of Mozambique", "", "^(\\+|(00))?((258\\-)|(258))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MM", "MMR", "104", "95", "", "缅甸", "Myanmar", "the Union of Myanmar", "", "^(\\+|(00))?((95\\-)|(95))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NA", "NAM", "516", "264", "", "纳米比亚", "Namibia", "the Republic of Namibia", "", "^(\\+|(00))?((264\\-)|(264))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NR", "NRU", "520", "674", "", "瑙鲁", "Nauru", "the Republic of Nauru", "", "^(\\+|(00))?((674\\-)|(674))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NP", "NPL", "524", "977", "", "尼泊尔", "Nepal", "Nepal", "", "^(\\+|(00))?((977\\-)|(977))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NL", "NLD", "528", "31", "", "荷兰", "Netherlands (the)", "the Kingdom of the Netherlands", "", "^(\\+|(00))?((31\\-)|(31))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AN", "ANT", "530", "599", "", "荷属安的列斯", "Netherlands Antilles (the)", "Netherlands Antilles (the)", "", "^(\\+|(00))?((599\\-)|(599))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NC", "NCL", "540", "687", "", "新喀里多尼亚", "New Caledonia", "New Caledonia", "", "^(\\+|(00))?((687\\-)|(687))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NZ", "NZL", "554", "64", "", "新西兰", "New Zealand", "New Zealand", "", "^(\\+|(00))?((64\\-)|(64))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NI", "NIC", "558", "505", "", "尼加拉瓜", "Nicaragua", "the Republic of Nicaragua", "", "^(\\+|(00))?((505\\-)|(505))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NE", "NER", "562", "227", "", "尼日尔", "Niger (the)", "the Republic of the Niger", "", "^(\\+|(00))?((227\\-)|(227))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NG", "NGA", "566", "234", "", "尼日利亚", "Nigeria", "the Federal Republic of Nigeria", "", "^(\\+|(00))?((234\\-)|(234))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NU", "NIU", "570", "683", "", "纽埃", "Niue", "the Republic of Niue", "", "^(\\+|(00))?((683\\-)|(683))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NF", "NFK", "574", "672", "", "诺福克岛", "Norfolk Island", "Norfolk Island", "", "^(\\+|(00))?((672\\-)|(672))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("MP", "MNP", "580", "1", "", "北马里亚纳", "Northern Mariana Islands (the)", "the Commonwealth of the Northern Mariana Islands", "", "^(\\+|(00))?((1\\-)|(1))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("NO", "NOR", "578", "47", "", "挪威", "Norway", "the Kingdom of Norway", "", "^(\\+|(00))?((47\\-)|(47))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("OM", "OMN", "512", "968", "", "阿曼", "Oman", "the Sultanate of Oman", "", "^(\\+|(00))?((968\\-)|(968))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PK", "PAK", "586", "92", "", "巴基斯坦", "Pakistan", "the Islamic Republic of Pakistan", "", "^(\\+|(00))?((92\\-)|(92))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PW", "PLW", "585", "680", "", "帕劳", "Palau", "the Republic of Palau", "", "^(\\+|(00))?((680\\-)|(680))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PS", "PSE", "275", "970", "", "巴勒斯坦", "Palestinian Territory (the Occupied)", "the Occupied Palestinian Territory", "", "^(\\+|(00))?((970\\-)|(970))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PA", "PAN", "591", "507", "", "巴拿马", "Panama", "the Republic of Panama", "", "^(\\+|(00))?((507\\-)|(507))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PG", "PNG", "598", "675", "", "巴布亚新几内亚", "Papua New Guinea", "Papua New Guinea", "", "^(\\+|(00))?((675\\-)|(675))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PY", "PRY", "600", "595", "", "巴拉圭", "Paraguay", "the Republic of Paraguay", "", "^(\\+|(00))?((595\\-)|(595))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PE", "PER", "604", "51", "", "秘鲁", "Peru", "the Republic of Peru", "", "^(\\+|(00))?((51\\-)|(51))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PH", "PHL", "608", "63", "", "菲律宾", "Philippines (the)", "the Republic of the Philippines", "", "^(\\+|(00))?((63\\-)|(63))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PN", "PCN", "612", "64", "", "皮特凯恩", "Pitcairn", "Pitcairn", "", "^(\\+|(00))?((64\\-)|(64))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PL", "POL", "616", "48", "", "波兰", "Poland", "the Republic of Poland", "", "^(\\+|(00))?((48\\-)|(48))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PT", "PRT", "620", "351", "", "葡萄牙", "Portugal", "the Portuguese Republic", "", "^(\\+|(00))?((351\\-)|(351))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PR", "PRI", "630", "1", "", "波多黎各", "Puerto Rico", "Puerto Rico", "", "^(\\+|(00))?((1\\-)|(1))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("QA", "QAT", "634", "974", "", "卡塔尔", "Qatar", "the State of Qatar", "", "^(\\+|(00))?((974\\-)|(974))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("RE", "REU", "638", "262", "", "留尼汪", "Réunion", "Réunion", "", "^(\\+|(00))?((262\\-)|(262))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("RO", "ROU", "642", "40", "", "罗马尼亚", "Romania", "Romania", "", "^(\\+|(00))?((40\\-)|(40))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("RU", "RUS", "643", "7", "", "俄罗斯联邦", "Russian Federation (the)", "the Russian Federation", "", "^(\\+|(00))?((7\\-)|(7))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("RW", "RWA", "646", "250", "", "卢旺达", "Rwanda", "the Republic of Rwanda", "", "^(\\+|(00))?((250\\-)|(250))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SH", "SHN", "654", "290", "", "圣赫勒拿", "Saint Helena", "Saint Helena", "", "^(\\+|(00))?((290\\-)|(290))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("KN", "KNA", "659", "1869", "", "圣基茨和尼维斯", "Saint Kitts and Nevis", "Saint Kitts and Nevis", "", "^(\\+|(00))?((1869\\-)|(1869))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LC", "LCA", "662", "1758", "", "圣卢西亚", "Saint Lucia", "Saint Lucia", "", "^(\\+|(00))?((1758\\-)|(1758))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("PM", "SPM", "666", "508", "", "圣皮埃尔和密克隆", "Saint Pierre and Miquelon", "Saint Pierre and Miquelon", "", "^(\\+|(00))?((508\\-)|(508))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("VC", "VCT", "670", "1784", "", "圣文森特和格林纳丁斯", "Saint Vincent and the Grenadines", "Saint Vincent and the Grenadines", "", "^(\\+|(00))?((1784\\-)|(1784))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("WS", "WSM", "882", "685", "", "萨摩亚", "Samoa", "the Independent State of Samoa", "", "^(\\+|(00))?((685\\-)|(685))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SM", "SMR", "674", "378", "", "圣马力诺", "San Marino", "the Republic of San Marino", "", "^(\\+|(00))?((378\\-)|(378))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ST", "STP", "678", "239", "", "圣多美和普林西比", "Sao Tome and Principe", "the Democratic Republic of Sao Tome and Principe", "", "^(\\+|(00))?((239\\-)|(239))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SA", "SAU", "682", "966", "", "沙特阿拉伯", "Saudi Arabia", "the Kingdom of Saudi Arabia", "", "^(\\+|(00))?((966\\-)|(966))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SN", "SEN", "686", "221", "", "塞内加尔", "Senegal", "the Republic of Senegal", "", "^(\\+|(00))?((221\\-)|(221))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("RS", "SRB", "688", "381", "", "塞尔维亚", "Serbia", "the Republic of Serbia", "", "^(\\+|(00))?((381\\-)|(381))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SC", "SYC", "690", "248", "", "塞舌尔", "Seychelles", "the Republic of Seychelles", "", "^(\\+|(00))?((248\\-)|(248))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SL", "SLE", "694", "232", "", "塞拉利昂", "Sierra Leone", "the Republic of Sierra Leone", "", "^(\\+|(00))?((232\\-)|(232))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SG", "SGP", "702", "65", "", "新加坡", "Singapore", "the Republic of Singapore", "", "^(\\+|(00))?((65\\-)|(65))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SK", "SVK", "703", "421", "", "斯洛伐克", "Slovakia", "the Slovak Republic", "", "^(\\+|(00))?((421\\-)|(421))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SI", "SVN", "705", "386", "", "斯洛文尼亚", "Slovenia", "the Republic of Slovenia", "", "^(\\+|(00))?((386\\-)|(386))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SB", "SLB", "090", "677", "", "所罗门群岛", "Solomon Islands (the)", "Solomon Islands (the)", "", "^(\\+|(00))?((677\\-)|(677))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SO", "SOM", "706", "252", "", "索马里", "Somalia", "the Somali Republic", "", "^(\\+|(00))?((252\\-)|(252))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ZA", "ZAF", "710", "27", "", "南非", "South Africa", "the Republic of South Africa", "", "^(\\+|(00))?((27\\-)|(27))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ES", "ESP", "724", "34", "", "西班牙", "Spain", "the Kingdom of Spain", "", "^(\\+|(00))?((34\\-)|(34))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("LK", "LKA", "144", "94", "", "斯里兰卡", "Sri Lanka", "the Democratic Socialist Republic of Sri Lanka", "", "^(\\+|(00))?((94\\-)|(94))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SD", "SDN", "736", "249", "", "苏丹", "Sudan (the)", "the Republic of the Sudan", "", "^(\\+|(00))?((249\\-)|(249))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SR", "SUR", "740", "597", "", "苏里南", "Suriname", "the Republic of Suriname", "", "^(\\+|(00))?((597\\-)|(597))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SJ", "SJM", "744", "47", "", "斯瓦尔巴岛和扬马延岛", "Svalbard and Jan Mayen", "Svalbard and Jan Mayen", "", "^(\\+|(00))?((47\\-)|(47))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SZ", "SWZ", "748", "268", "", "斯威士兰", "Swaziland", "the Kingdom of Swaziland", "", "^(\\+|(00))?((268\\-)|(268))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SE", "SWE", "752", "46", "", "瑞典", "Sweden", "the Kingdom of Sweden", "", "^(\\+|(00))?((46\\-)|(46))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("CH", "CHE", "756", "41", "", "瑞士", "Switzerland", "the Swiss Confederation", "", "^(\\+|(00))?((41\\-)|(41))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("SY", "SYR", "760", "963", "", "叙利亚", "Syrian Arab Republic (the)", "the Syrian Arab Republic", "", "^(\\+|(00))?((963\\-)|(963))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TW", "TWN", "158", "886", "", "中国台湾", "Taiwan (Province of China)", "Taiwan (Province of China)", "", "^(\\+|(00))?((886\\-)|(886))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TJ", "TJK", "762", "992", "", "塔吉克斯坦", "Tajikistan", "the Republic of Tajikistan", "", "^(\\+|(00))?((992\\-)|(992))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TZ", "TZA", "834", "255", "", "坦桑尼亚", "Tanzania,United Republic of", "the United Republic of Tanzania", "", "^(\\+|(00))?((255\\-)|(255))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TH", "THA", "764", "66", "", "泰国", "Thailand", "the Kingdom of Thailand", "", "^(\\+|(00))?((66\\-)|(66))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TL", "TLS", "626", "670", "", "东帝汶", "Timor-Leste", "the Democratic Republic of Timor-Leste", "", "^(\\+|(00))?((670\\-)|(670))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TG", "TGO", "768", "228", "", "多哥", "Togo", "the Togolese Republic", "", "^(\\+|(00))?((228\\-)|(228))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TK", "TKL", "772", "690", "", "托克劳", "Tokelau", "Tokelau", "", "^(\\+|(00))?((690\\-)|(690))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TO", "TON", "776", "676", "", "汤加", "Tonga", "the Kingdom of Tonga", "", "^(\\+|(00))?((676\\-)|(676))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TT", "TTO", "780", "1868", "", "特立尼达和多巴哥", "Trinidad and Tobago", "the Republic of Trinidad and Tobago", "", "^(\\+|(00))?((1868\\-)|(1868))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TN", "TUN", "788", "216", "", "突尼斯", "Tunisia", "the Republic of Tunisia", "", "^(\\+|(00))?((216\\-)|(216))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TR", "TUR", "792", "90", "", "土耳其", "Turkey", "the Republic of Turkey", "", "^(\\+|(00))?((90\\-)|(90))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TM", "TKM", "795", "993", "", "土库曼斯坦", "Turkmenistan", "Turkmenistan", "", "^(\\+|(00))?((993\\-)|(993))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TC", "TCA", "796", "1649", "", "特克斯和凯科斯群岛", "Turks and Caicos Islands (the)", "Turks and Caicos Islands (the)", "", "^(\\+|(00))?((1649\\-)|(1649))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("TV", "TUV", "798", "688", "", "图瓦卢", "Tuvalu", "Tuvalu", "", "^(\\+|(00))?((688\\-)|(688))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("UG", "UGA", "800", "256", "", "乌干达", "Uganda", "the Republic of Uganda", "", "^(\\+|(00))?((256\\-)|(256))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("UA", "UKR", "804", "380", "", "乌克兰", "Ukraine", "Ukraine", "", "^(\\+|(00))?((380\\-)|(380))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("AE", "ARE", "784", "971", "", "阿联酋", "United Arab Emirates (the)", "the United Arab Emirates", "", "^(\\+|(00))?((971\\-)|(971))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("GB", "GBR", "826", "44", "", "英国", "United Kingdom (the)", "the United Kingdom of Great Britain and Northern Ireland", "", "^(\\+|(00))?((44\\-)|(44))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("US", "USA", "840", "1", "", "美国", "United States (the)", "the United States of America", "", "^(\\+|(00))?((1\\-)|(1))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("UM", "UMI", "581", "1", "", "美国本土外小岛屿", "United States Minor Outlying Islands (the)", "United States Minor Outlying Islands (the)", "", "^(\\+|(00))?((1\\-)|(1))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("UY", "URY", "858", "598", "", "乌拉圭", "Uruguay", "the Eastern Republic of Uruguay", "", "^(\\+|(00))?((598\\-)|(598))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("UZ", "UZB", "860", "998", "", "乌兹别克斯坦", "Uzbekistan", "the Republic of Uzbekistan", "", "^(\\+|(00))?((998\\-)|(998))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("VU", "VUT", "548", "678", "", "瓦努阿图", "Vanuatu", "the Republic of Vanuatu", "", "^(\\+|(00))?((678\\-)|(678))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("VE", "VEN", "862", "58", "", "委内瑞拉", "Venezuela", "the Bolivarian Republic of Venezuela", "", "^(\\+|(00))?((58\\-)|(58))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("VN", "VNM", "704", "84", "", "越南", "Viet Nam", "the Socialist Republic of Viet Nam", "", "^(\\+|(00))?((84\\-)|(84))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("VG", "VGB", "092", "1284", "", "英属维尔京群岛", "Virgin Islands (British)", "British Virgin Islands (the)", "", "^(\\+|(00))?((1284\\-)|(1284))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("VI", "VIR", "850", "1340", "", "美属维尔京群岛", "Virgin Islands (U.S.)", "the Virgin Islands of the United States", "", "^(\\+|(00))?((1340\\-)|(1340))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("WF", "WLF", "876", "681", "", "瓦利斯和富图纳", "Wallis and Futuna", "Wallis and Futuna Islands", "", "^(\\+|(00))?((681\\-)|(681))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("EH", "ESH", "732", "212", "", "西撒哈拉", "Western Sahara", "Western Sahara", "", "^(\\+|(00))?((212\\-)|(212))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("YE", "YEM", "887", "967", "", "也门", "Yemen", "the Republic of Yemen", "", "^(\\+|(00))?((967\\-)|(967))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("YU", "YUG", "891", "381", "", "南斯拉夫", "Yugoslavia", "Yugoslavia", "", "^(\\+|(00))?((381\\-)|(381))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ZM", "ZMB", "894", "260", "", "赞比亚", "Zambia", "the Republic of Zambia", "", "^(\\+|(00))?((260\\-)|(260))\\d[\\d\\-]+\\d$", "", ""));
		countries.add(new CountryData("ZW", "ZWE", "716", "263", "", "津巴布韦", "Zimbabwe", "the Republic of Zimbabwe", "", "^(\\+|(00))?((263\\-)|(263))\\d[\\d\\-]+\\d$", "", ""));

		CountriesSorter sorter = new CountriesSorter();
		countries = sorter.bubble(countries, JUtilSorter.ASC);
	}

	/**
	 *
	 * @return
	 */
	public static List<CountryData> getCountries(){
		return countries;
	}

	/**
	 *
	 * @param countryCode
	 * @return
	 */
	public static CountryData getCountry(String countryCode){
		for(int i=0;i<countries.size();i++){
			CountryData c=countries.get(i);
			if(c.code.equals(countryCode)
					||c.mobileCode.equals(countryCode)) return c;
		}
		return null;
	}

	/**
	 *
	 * @param str
	 * @return
	 */
	public static CountryData findCountry(String str){
		for(int i=0;i<countries.size();i++){
			CountryData c=countries.get(i);
			if(c.code.equals(str)
					||c.mobileCode.equals(str)
					||c.cnName.equals(str)
					||c.enName.equals(str)
					||c.enNameFull.equals(str)) return c;
		}
		return null;
	}
	
	/**
	 * 解析电话号码详情[区号部分,不含区号部分]
	 * @param num
	 * @return
	 */
	public static String[] getPhoneNumberDetail(String num){
		if(JUtilString.isBlank(num)) return new String[]{"",num};

		if(num.startsWith("+")) num=num.substring(1);

		String[] detail=num.split("\\-");
		if(detail.length==1) return new String[]{"",num};

		CountryData c=getCountry(detail[0]);
		if(c==null && detail[0].startsWith("00")) c=getCountry(detail[0].substring(2));
		if(c==null) return new String[]{"",num};
		
		return new String[]{detail[0], num.substring(num.indexOf("-")+1)};
	}

	/**
	 * 调整为标准格式
	 * @param num
	 * @return
	 */
	public static String formatPhoneNumber(String num){
		String[] cells = getPhoneNumberDetail(num);
		if(JUtilString.isBlank(cells[0])) return "+"+Countries.DEFAULT_MOBILE_CODE+"-"+cells[1];
		else return (cells[0].startsWith("00")?"":"+")+cells[0]+"-"+cells[1];
	}

	/**
	 *
	 * @param num
	 * @return
	 */
	public static boolean isPhoneNumberValid(String num){
		if(JUtilString.isBlank(num)) return false;

		String[] detail = getPhoneNumberDetail(num);

		CountryData c=getCountry(detail[0]);
		if(c==null && detail[0].startsWith("00")) c=getCountry(detail[0].substring(2));
		if(c==null) return isPhoneNumberValid(Countries.DEFAULT_MOBILE_CODE, num);

		boolean valid=true;
		if(!(detail[0]+"-"+detail[1]).matches(c.RE)) valid=false;
		if(!detail[1].matches("^\\d{6,20}$")) valid=false;//6~20个数字
		if(!JUtilString.isBlank(c.RE2) && !detail[1].matches(c.RE2)) valid=false;//除国际区号外的部分不符合规则
		if(!JUtilString.isBlank(c.RE3) && detail.length>2 && !detail[2].matches(c.RE3)) valid=false;//分机号不符合规则
		return valid;
	}

	/**
	 *
	 * @param mobileCode
	 * @param num
	 * @return
	 */
	public static boolean isPhoneNumberValid(String mobileCode, String num){
		if(JUtilString.isBlank(mobileCode)) return false;
		if(JUtilString.isBlank(num)) return false;
		
		String[] detail=new String[]{mobileCode, num};

		CountryData c=getCountry(detail[0]);
		if(c==null && detail[0].startsWith("00")) c=getCountry(detail[0].substring(2));
		if(c==null) return false;

		return (detail[0]+"-"+detail[1]).matches(c.RE);
	}

	/**
	 *
	 */
	private static void genJs(){
		StringBuffer s=new StringBuffer();
		for(int i=0; i<countries.size(); i++){
			CountryData c=countries.get(i);
			if(i>0) s.append(",\r\n");
			s.append("\r\n{\r\n");
			s.append("\tvalue:\""+c.code+"\",\r\n");
			s.append("\tphonePrefix:\""+c.mobileCode+"\",\r\n");
			s.append("\tcnName:\""+c.cnName+"\",\r\n");
			s.append("\tcnPinyin:\""+ JUtilPinYin.toPinYin(c.cnName," ", true) +"\",\r\n");
			s.append("\tenName:\""+c.enName+"\",\r\n");
			s.append("\tgroup:\"\",\r\n");
			s.append("\tRE:/"+c.RE.substring(1, c.RE.length()-1)+"/\r\n");
			s.append("}");
		}
		System.out.println(s);
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		//^\\d{3,6}\\-?\\d{5,10}\\-?\\d{0,6}$
		System.out.println(formatPhoneNumber("17623391339"));
		System.out.println(formatPhoneNumber("+86-17623391339"));
		System.out.println(formatPhoneNumber("0086-17623391339"));

		System.out.println(isPhoneNumberValid("17623391339"));
		System.out.println(isPhoneNumberValid("+86-17623391339"));
		System.out.println(isPhoneNumberValid("0086-17623391339"));

		genJs();
	}
}
