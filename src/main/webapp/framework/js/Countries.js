//全球各国及对应的手机/电话格式校验
let Countries={
    DEFAULT_COUNTRY_CODE:"CN",
    DEFAULT_MOBILE_CODE:"86",
    areas:[
        {
            value:"CN",
            phonePrefix:"86",
            cnName:"中国",
            cnPinyin:"Zhong Guo",
            enName:"China",
            group:"",
            RE:/(\+|(00))?((86\-)|(86))\d[\d\-]+\d/
        },

        {
            value:"TW",
            phonePrefix:"886",
            cnName:"中国台湾",
            cnPinyin:"Zhong Guo Tai Wan",
            enName:"Taiwan (Province of China)",
            group:"",
            RE:/(\+|(00))?((886\-)|(886))\d[\d\-]+\d/
        },

        {
            value:"MO",
            phonePrefix:"853",
            cnName:"中国澳门",
            cnPinyin:"Zhong Guo Ao Men",
            enName:"Macao",
            group:"",
            RE:/(\+|(00))?((853\-)|(853))\d[\d\-]+\d/
        },

        {
            value:"HK",
            phonePrefix:"852",
            cnName:"中国香港",
            cnPinyin:"Zhong Guo Xiang Gang",
            enName:"Hong Kong",
            group:"",
            RE:/(\+|(00))?((852\-)|(852))\d[\d\-]+\d/
        },

        {
            value:"AF",
            phonePrefix:"93",
            cnName:"阿富汗",
            cnPinyin:"A Fu Han",
            enName:"Afghanistan",
            group:"",
            RE:/(\+|(00))?((93\-)|(93))\d[\d\-]+\d/
        },

        {
            value:"AX",
            phonePrefix:"35818",
            cnName:"奥兰群岛",
            cnPinyin:"Ao Lan Qun Dao",
            enName:"Aland Islands",
            group:"",
            RE:/(\+|(00))?((35818\-)|(35818))\d[\d\-]+\d/
        },

        {
            value:"AL",
            phonePrefix:"355",
            cnName:"阿尔巴尼亚",
            cnPinyin:"A Er Ba Ni Ya",
            enName:"Albania",
            group:"",
            RE:/(\+|(00))?((355\-)|(355))\d[\d\-]+\d/
        },

        {
            value:"DZ",
            phonePrefix:"213",
            cnName:"阿尔及利亚",
            cnPinyin:"A Er Ji Li Ya",
            enName:"Algeria",
            group:"",
            RE:/(\+|(00))?((213\-)|(213))\d[\d\-]+\d/
        },

        {
            value:"AS",
            phonePrefix:"1",
            cnName:"美属萨摩亚",
            cnPinyin:"Mei Shu Sa Mo Ya",
            enName:"American Samoa",
            group:"",
            RE:/(\+|(00))?((1\-)|(1))\d[\d\-]+\d/
        },

        {
            value:"AD",
            phonePrefix:"376",
            cnName:"安道尔",
            cnPinyin:"An Dao Er",
            enName:"Andorra",
            group:"",
            RE:/(\+|(00))?((376\-)|(376))\d[\d\-]+\d/
        },

        {
            value:"AO",
            phonePrefix:"244",
            cnName:"安哥拉",
            cnPinyin:"An Ge La",
            enName:"Angola",
            group:"",
            RE:/(\+|(00))?((244\-)|(244))\d[\d\-]+\d/
        },

        {
            value:"AI",
            phonePrefix:"1264",
            cnName:"安圭拉",
            cnPinyin:"An Gui La",
            enName:"Anguilla",
            group:"",
            RE:/(\+|(00))?((1264\-)|(1264))\d[\d\-]+\d/
        },

        {
            value:"AQ",
            phonePrefix:"64672",
            cnName:"南极洲",
            cnPinyin:"Nan Ji Zhou",
            enName:"Antarctica",
            group:"",
            RE:/(\+|(00))?((64672\-)|(64672))\d[\d\-]+\d/
        },

        {
            value:"AG",
            phonePrefix:"1268",
            cnName:"安提瓜和巴布达",
            cnPinyin:"An Ti Gua He Ba Bu Da",
            enName:"Antigua and Barbuda",
            group:"",
            RE:/(\+|(00))?((1268\-)|(1268))\d[\d\-]+\d/
        },

        {
            value:"AR",
            phonePrefix:"54",
            cnName:"阿根廷",
            cnPinyin:"A Gen Ting",
            enName:"Argentina",
            group:"",
            RE:/(\+|(00))?((54\-)|(54))\d[\d\-]+\d/
        },

        {
            value:"AM",
            phonePrefix:"374",
            cnName:"亚美尼亚",
            cnPinyin:"Ya Mei Ni Ya",
            enName:"Armenia",
            group:"",
            RE:/(\+|(00))?((374\-)|(374))\d[\d\-]+\d/
        },

        {
            value:"AW",
            phonePrefix:"297",
            cnName:"阿鲁巴",
            cnPinyin:"A Lu Ba",
            enName:"Aruba",
            group:"",
            RE:/(\+|(00))?((297\-)|(297))\d[\d\-]+\d/
        },

        {
            value:"AU",
            phonePrefix:"61",
            cnName:"澳大利亚",
            cnPinyin:"Ao Da Li Ya",
            enName:"Australia",
            group:"",
            RE:/(\+|(00))?((61\-)|(61))\d[\d\-]+\d/
        },

        {
            value:"AT",
            phonePrefix:"43",
            cnName:"奥地利",
            cnPinyin:"Ao Di Li",
            enName:"Austria",
            group:"",
            RE:/(\+|(00))?((43\-)|(43))\d[\d\-]+\d/
        },

        {
            value:"AZ",
            phonePrefix:"994",
            cnName:"阿塞拜疆",
            cnPinyin:"A Sai Bai Jiang",
            enName:"Azerbaijan",
            group:"",
            RE:/(\+|(00))?((994\-)|(994))\d[\d\-]+\d/
        },

        {
            value:"BS",
            phonePrefix:"1242",
            cnName:"巴哈马",
            cnPinyin:"Ba Ha Ma",
            enName:"Bahamas (The)",
            group:"",
            RE:/(\+|(00))?((1242\-)|(1242))\d[\d\-]+\d/
        },

        {
            value:"BH",
            phonePrefix:"973",
            cnName:"巴林",
            cnPinyin:"Ba Lin",
            enName:"Bahrain",
            group:"",
            RE:/(\+|(00))?((973\-)|(973))\d[\d\-]+\d/
        },

        {
            value:"BD",
            phonePrefix:"880",
            cnName:"孟加拉国",
            cnPinyin:"Meng Jia La Guo",
            enName:"Bangladesh",
            group:"",
            RE:/(\+|(00))?((880\-)|(880))\d[\d\-]+\d/
        },

        {
            value:"BB",
            phonePrefix:"1246",
            cnName:"巴巴多斯",
            cnPinyin:"Ba Ba Duo Si",
            enName:"Barbados",
            group:"",
            RE:/(\+|(00))?((1246\-)|(1246))\d[\d\-]+\d/
        },

        {
            value:"BY",
            phonePrefix:"375",
            cnName:"白俄罗斯",
            cnPinyin:"Bai E Luo Si",
            enName:"Belarus",
            group:"",
            RE:/(\+|(00))?((375\-)|(375))\d[\d\-]+\d/
        },

        {
            value:"BE",
            phonePrefix:"32",
            cnName:"比利时",
            cnPinyin:"Bi Li Shi",
            enName:"Belgium",
            group:"",
            RE:/(\+|(00))?((32\-)|(32))\d[\d\-]+\d/
        },

        {
            value:"BZ",
            phonePrefix:"501",
            cnName:"伯利兹",
            cnPinyin:"Bo Li Zi",
            enName:"Belize",
            group:"",
            RE:/(\+|(00))?((501\-)|(501))\d[\d\-]+\d/
        },

        {
            value:"BJ",
            phonePrefix:"229",
            cnName:"贝宁",
            cnPinyin:"Bei Ning",
            enName:"Benin",
            group:"",
            RE:/(\+|(00))?((229\-)|(229))\d[\d\-]+\d/
        },

        {
            value:"BM",
            phonePrefix:"1441",
            cnName:"百慕大",
            cnPinyin:"Bai Mu Da",
            enName:"Bermuda",
            group:"",
            RE:/(\+|(00))?((1441\-)|(1441))\d[\d\-]+\d/
        },

        {
            value:"BT",
            phonePrefix:"975",
            cnName:"不丹",
            cnPinyin:"Bu Dan",
            enName:"Bhutan",
            group:"",
            RE:/(\+|(00))?((975\-)|(975))\d[\d\-]+\d/
        },

        {
            value:"BO",
            phonePrefix:"591",
            cnName:"玻利维亚",
            cnPinyin:"Bo Li Wei Ya",
            enName:"Bolivia",
            group:"",
            RE:/(\+|(00))?((591\-)|(591))\d[\d\-]+\d/
        },

        {
            value:"BA",
            phonePrefix:"387",
            cnName:"波黑",
            cnPinyin:"Bo Hei",
            enName:"Bosnia and Herzegovina",
            group:"",
            RE:/(\+|(00))?((387\-)|(387))\d[\d\-]+\d/
        },

        {
            value:"BW",
            phonePrefix:"267",
            cnName:"博茨瓦纳",
            cnPinyin:"Bo Ci Wa Na",
            enName:"Botswana",
            group:"",
            RE:/(\+|(00))?((267\-)|(267))\d[\d\-]+\d/
        },

        {
            value:"BV",
            phonePrefix:"47",
            cnName:"布维岛",
            cnPinyin:"Bu Wei Dao",
            enName:"Bouvet Island",
            group:"",
            RE:/(\+|(00))?((47\-)|(47))\d[\d\-]+\d/
        },

        {
            value:"BR",
            phonePrefix:"55",
            cnName:"巴西",
            cnPinyin:"Ba Xi",
            enName:"Brazil",
            group:"",
            RE:/(\+|(00))?((55\-)|(55))\d[\d\-]+\d/
        },

        {
            value:"IO",
            phonePrefix:"44",
            cnName:"英属印度洋领地",
            cnPinyin:"Ying Shu Yin Du Yang Ling Di",
            enName:"British Indian Ocean Territory (the)",
            group:"",
            RE:/(\+|(00))?((44\-)|(44))\d[\d\-]+\d/
        },

        {
            value:"BN",
            phonePrefix:"673",
            cnName:"文莱",
            cnPinyin:"Wen Lai",
            enName:"Brunei Darussalam",
            group:"",
            RE:/(\+|(00))?((673\-)|(673))\d[\d\-]+\d/
        },

        {
            value:"BG",
            phonePrefix:"359",
            cnName:"保加利亚",
            cnPinyin:"Bao Jia Li Ya",
            enName:"Bulgaria",
            group:"",
            RE:/(\+|(00))?((359\-)|(359))\d[\d\-]+\d/
        },

        {
            value:"BF",
            phonePrefix:"226",
            cnName:"布基纳法索",
            cnPinyin:"Bu Ji Na Fa Suo",
            enName:"Burkina Faso",
            group:"",
            RE:/(\+|(00))?((226\-)|(226))\d[\d\-]+\d/
        },

        {
            value:"BI",
            phonePrefix:"257",
            cnName:"布隆迪",
            cnPinyin:"Bu Long Di",
            enName:"Burundi",
            group:"",
            RE:/(\+|(00))?((257\-)|(257))\d[\d\-]+\d/
        },

        {
            value:"KH",
            phonePrefix:"855",
            cnName:"柬埔寨",
            cnPinyin:"Jian Pu Zhai",
            enName:"Cambodia",
            group:"",
            RE:/(\+|(00))?((855\-)|(855))\d[\d\-]+\d/
        },

        {
            value:"CM",
            phonePrefix:"237",
            cnName:"喀麦隆",
            cnPinyin:"Ka Mai Long",
            enName:"Cameroon",
            group:"",
            RE:/(\+|(00))?((237\-)|(237))\d[\d\-]+\d/
        },

        {
            value:"CA",
            phonePrefix:"1",
            cnName:"加拿大",
            cnPinyin:"Jia Na Da",
            enName:"Canada",
            group:"",
            RE:/(\+|(00))?((1\-)|(1))\d[\d\-]+\d/
        },

        {
            value:"CV",
            phonePrefix:"238",
            cnName:"佛得角",
            cnPinyin:"Fo De Jiao",
            enName:"Cape Verde",
            group:"",
            RE:/(\+|(00))?((238\-)|(238))\d[\d\-]+\d/
        },

        {
            value:"KY",
            phonePrefix:"1345",
            cnName:"开曼群岛",
            cnPinyin:"Kai Man Qun Dao",
            enName:"Cayman Islands (the)",
            group:"",
            RE:/(\+|(00))?((1345\-)|(1345))\d[\d\-]+\d/
        },

        {
            value:"CF",
            phonePrefix:"236",
            cnName:"中非",
            cnPinyin:"Zhong Fei",
            enName:"Central African Republic (the)",
            group:"",
            RE:/(\+|(00))?((236\-)|(236))\d[\d\-]+\d/
        },

        {
            value:"TD",
            phonePrefix:"235",
            cnName:"乍得",
            cnPinyin:"Zha De",
            enName:"Chad",
            group:"",
            RE:/(\+|(00))?((235\-)|(235))\d[\d\-]+\d/
        },

        {
            value:"CL",
            phonePrefix:"56",
            cnName:"智利",
            cnPinyin:"Zhi Li",
            enName:"Chile",
            group:"",
            RE:/(\+|(00))?((56\-)|(56))\d[\d\-]+\d/
        },

        {
            value:"CX",
            phonePrefix:"618",
            cnName:"圣诞岛",
            cnPinyin:"Sheng Dan Dao",
            enName:"Christmas Island",
            group:"",
            RE:/(\+|(00))?((618\-)|(618))\d[\d\-]+\d/
        },

        {
            value:"CC",
            phonePrefix:"61891",
            cnName:"科科斯（基林）群岛",
            cnPinyin:"Ke Ke Si （ Ji Lin ） Qun Dao",
            enName:"Cocos (Keeling) Islands (the)",
            group:"",
            RE:/(\+|(00))?((61891\-)|(61891))\d[\d\-]+\d/
        },

        {
            value:"CO",
            phonePrefix:"57",
            cnName:"哥伦比亚",
            cnPinyin:"Ge Lun Bi Ya",
            enName:"Colombia",
            group:"",
            RE:/(\+|(00))?((57\-)|(57))\d[\d\-]+\d/
        },

        {
            value:"KM",
            phonePrefix:"269",
            cnName:"科摩罗",
            cnPinyin:"Ke Mo Luo",
            enName:"Comoros",
            group:"",
            RE:/(\+|(00))?((269\-)|(269))\d[\d\-]+\d/
        },

        {
            value:"CG",
            phonePrefix:"242",
            cnName:"刚果（布）",
            cnPinyin:"Gang Guo （ Bu ）",
            enName:"Congo",
            group:"",
            RE:/(\+|(00))?((242\-)|(242))\d[\d\-]+\d/
        },

        {
            value:"CD",
            phonePrefix:"243",
            cnName:"刚果（金）",
            cnPinyin:"Gang Guo （ Jin ）",
            enName:"Congo (the Democratic Republic of the)",
            group:"",
            RE:/(\+|(00))?((243\-)|(243))\d[\d\-]+\d/
        },

        {
            value:"CK",
            phonePrefix:"682",
            cnName:"库克群岛",
            cnPinyin:"Ku Ke Qun Dao",
            enName:"Cook Islands (the)",
            group:"",
            RE:/(\+|(00))?((682\-)|(682))\d[\d\-]+\d/
        },

        {
            value:"CR",
            phonePrefix:"506",
            cnName:"哥斯达黎加",
            cnPinyin:"Ge Si Da Li Jia",
            enName:"Costa Rica",
            group:"",
            RE:/(\+|(00))?((506\-)|(506))\d[\d\-]+\d/
        },

        {
            value:"CI",
            phonePrefix:"225",
            cnName:"科特迪瓦",
            cnPinyin:"Ke Te Di Wa",
            enName:"C?te d'Ivoire",
            group:"",
            RE:/(\+|(00))?((225\-)|(225))\d[\d\-]+\d/
        },

        {
            value:"HR",
            phonePrefix:"385",
            cnName:"克罗地亚",
            cnPinyin:"Ke Luo Di Ya",
            enName:"Croatia",
            group:"",
            RE:/(\+|(00))?((385\-)|(385))\d[\d\-]+\d/
        },

        {
            value:"CU",
            phonePrefix:"53",
            cnName:"古巴",
            cnPinyin:"Gu Ba",
            enName:"Cuba",
            group:"",
            RE:/(\+|(00))?((53\-)|(53))\d[\d\-]+\d/
        },

        {
            value:"CY",
            phonePrefix:"357",
            cnName:"塞浦路斯",
            cnPinyin:"Sai Pu Lu Si",
            enName:"Cyprus",
            group:"",
            RE:/(\+|(00))?((357\-)|(357))\d[\d\-]+\d/
        },

        {
            value:"CZ",
            phonePrefix:"420",
            cnName:"捷克",
            cnPinyin:"Jie Ke",
            enName:"Czech Republic (the)",
            group:"",
            RE:/(\+|(00))?((420\-)|(420))\d[\d\-]+\d/
        },

        {
            value:"DK",
            phonePrefix:"45",
            cnName:"丹麦",
            cnPinyin:"Dan Mai",
            enName:"Denmark",
            group:"",
            RE:/(\+|(00))?((45\-)|(45))\d[\d\-]+\d/
        },

        {
            value:"DJ",
            phonePrefix:"253",
            cnName:"吉布提",
            cnPinyin:"Ji Bu Ti",
            enName:"Djibouti",
            group:"",
            RE:/(\+|(00))?((253\-)|(253))\d[\d\-]+\d/
        },

        {
            value:"DM",
            phonePrefix:"1767",
            cnName:"多米尼克",
            cnPinyin:"Duo Mi Ni Ke",
            enName:"Dominica",
            group:"",
            RE:/(\+|(00))?((1767\-)|(1767))\d[\d\-]+\d/
        },

        {
            value:"DO",
            phonePrefix:"18",
            cnName:"多米尼加",
            cnPinyin:"Duo Mi Ni Jia",
            enName:"Dominican Republic (the)",
            group:"",
            RE:/(\+|(00))?((18\-)|(18))\d[\d\-]+\d/
        },

        {
            value:"EC",
            phonePrefix:"593",
            cnName:"厄瓜多尔",
            cnPinyin:"E Gua Duo Er",
            enName:"Ecuador",
            group:"",
            RE:/(\+|(00))?((593\-)|(593))\d[\d\-]+\d/
        },

        {
            value:"EG",
            phonePrefix:"20",
            cnName:"埃及",
            cnPinyin:"Ai Ji",
            enName:"Egypt",
            group:"",
            RE:/(\+|(00))?((20\-)|(20))\d[\d\-]+\d/
        },

        {
            value:"SV",
            phonePrefix:"503",
            cnName:"萨尔瓦多",
            cnPinyin:"Sa Er Wa Duo",
            enName:"El Salvador",
            group:"",
            RE:/(\+|(00))?((503\-)|(503))\d[\d\-]+\d/
        },

        {
            value:"GQ",
            phonePrefix:"240",
            cnName:"赤道几内亚",
            cnPinyin:"Chi Dao Ji Nei Ya",
            enName:"Equatorial Guinea",
            group:"",
            RE:/(\+|(00))?((240\-)|(240))\d[\d\-]+\d/
        },

        {
            value:"ER",
            phonePrefix:"291",
            cnName:"厄立特里亚",
            cnPinyin:"E Li Te Li Ya",
            enName:"Eritrea",
            group:"",
            RE:/(\+|(00))?((291\-)|(291))\d[\d\-]+\d/
        },

        {
            value:"EE",
            phonePrefix:"372",
            cnName:"爱沙尼亚",
            cnPinyin:"Ai Sha Ni Ya",
            enName:"Estonia",
            group:"",
            RE:/(\+|(00))?((372\-)|(372))\d[\d\-]+\d/
        },

        {
            value:"ET",
            phonePrefix:"251",
            cnName:"埃塞俄比亚",
            cnPinyin:"Ai Sai E Bi Ya",
            enName:"Ethiopia",
            group:"",
            RE:/(\+|(00))?((251\-)|(251))\d[\d\-]+\d/
        },

        {
            value:"FK",
            phonePrefix:"500",
            cnName:"福克兰群岛（马尔维纳斯）",
            cnPinyin:"Fu Ke Lan Qun Dao （ Ma Er Wei Na Si ）",
            enName:"Falkland Islands (the) [Malvinas]",
            group:"",
            RE:/(\+|(00))?((500\-)|(500))\d[\d\-]+\d/
        },

        {
            value:"FO",
            phonePrefix:"298",
            cnName:"法罗群岛",
            cnPinyin:"Fa Luo Qun Dao",
            enName:"Faroe Islands (the)",
            group:"",
            RE:/(\+|(00))?((298\-)|(298))\d[\d\-]+\d/
        },

        {
            value:"FJ",
            phonePrefix:"679",
            cnName:"斐济",
            cnPinyin:"Fei Ji",
            enName:"Fiji",
            group:"",
            RE:/(\+|(00))?((679\-)|(679))\d[\d\-]+\d/
        },

        {
            value:"FI",
            phonePrefix:"358",
            cnName:"芬兰",
            cnPinyin:"Fen Lan",
            enName:"Finland",
            group:"",
            RE:/(\+|(00))?((358\-)|(358))\d[\d\-]+\d/
        },

        {
            value:"FR",
            phonePrefix:"33",
            cnName:"法国",
            cnPinyin:"Fa Guo",
            enName:"France",
            group:"",
            RE:/(\+|(00))?((33\-)|(33))\d[\d\-]+\d/
        },

        {
            value:"GF",
            phonePrefix:"594",
            cnName:"法属圭亚那",
            cnPinyin:"Fa Shu Gui Ya Nei",
            enName:"French Guiana",
            group:"",
            RE:/(\+|(00))?((594\-)|(594))\d[\d\-]+\d/
        },

        {
            value:"PF",
            phonePrefix:"689",
            cnName:"法属波利尼西亚",
            cnPinyin:"Fa Shu Bo Li Ni Xi Ya",
            enName:"French Polynesia",
            group:"",
            RE:/(\+|(00))?((689\-)|(689))\d[\d\-]+\d/
        },

        {
            value:"TF",
            phonePrefix:"33",
            cnName:"法属南部领地",
            cnPinyin:"Fa Shu Nan Bu Ling Di",
            enName:"French Southern Territories (the)",
            group:"",
            RE:/(\+|(00))?((33\-)|(33))\d[\d\-]+\d/
        },

        {
            value:"GA",
            phonePrefix:"241",
            cnName:"加蓬",
            cnPinyin:"Jia Peng",
            enName:"Gabon",
            group:"",
            RE:/(\+|(00))?((241\-)|(241))\d[\d\-]+\d/
        },

        {
            value:"GM",
            phonePrefix:"220",
            cnName:"冈比亚",
            cnPinyin:"Gang Bi Ya",
            enName:"Gambia (The)",
            group:"",
            RE:/(\+|(00))?((220\-)|(220))\d[\d\-]+\d/
        },

        {
            value:"GE",
            phonePrefix:"995",
            cnName:"格鲁吉亚",
            cnPinyin:"Ge Lu Ji Ya",
            enName:"Georgia",
            group:"",
            RE:/(\+|(00))?((995\-)|(995))\d[\d\-]+\d/
        },

        {
            value:"DE",
            phonePrefix:"49",
            cnName:"德国",
            cnPinyin:"De Guo",
            enName:"Germany",
            group:"",
            RE:/(\+|(00))?((49\-)|(49))\d[\d\-]+\d/
        },

        {
            value:"GH",
            phonePrefix:"233",
            cnName:"加纳",
            cnPinyin:"Jia Na",
            enName:"Ghana",
            group:"",
            RE:/(\+|(00))?((233\-)|(233))\d[\d\-]+\d/
        },

        {
            value:"GI",
            phonePrefix:"350",
            cnName:"直布罗陀",
            cnPinyin:"Zhi Bu Luo Tuo",
            enName:"Gibraltar",
            group:"",
            RE:/(\+|(00))?((350\-)|(350))\d[\d\-]+\d/
        },

        {
            value:"GR",
            phonePrefix:"30",
            cnName:"希腊",
            cnPinyin:"Xi La",
            enName:"Greece",
            group:"",
            RE:/(\+|(00))?((30\-)|(30))\d[\d\-]+\d/
        },

        {
            value:"GL",
            phonePrefix:"299",
            cnName:"格陵兰",
            cnPinyin:"Ge Ling Lan",
            enName:"Greenland",
            group:"",
            RE:/(\+|(00))?((299\-)|(299))\d[\d\-]+\d/
        },

        {
            value:"GD",
            phonePrefix:"1473",
            cnName:"格林纳达",
            cnPinyin:"Ge Lin Na Da",
            enName:"Grenada",
            group:"",
            RE:/(\+|(00))?((1473\-)|(1473))\d[\d\-]+\d/
        },

        {
            value:"GP",
            phonePrefix:"590",
            cnName:"瓜德罗普",
            cnPinyin:"Gua De Luo Pu",
            enName:"Guadeloupe",
            group:"",
            RE:/(\+|(00))?((590\-)|(590))\d[\d\-]+\d/
        },

        {
            value:"GU",
            phonePrefix:"1",
            cnName:"关岛",
            cnPinyin:"Guan Dao",
            enName:"Guam",
            group:"",
            RE:/(\+|(00))?((1\-)|(1))\d[\d\-]+\d/
        },

        {
            value:"GT",
            phonePrefix:"502",
            cnName:"危地马拉",
            cnPinyin:"Wei Di Ma La",
            enName:"Guatemala",
            group:"",
            RE:/(\+|(00))?((502\-)|(502))\d[\d\-]+\d/
        },

        {
            value:"GG",
            phonePrefix:"44",
            cnName:"格恩西岛",
            cnPinyin:"Ge En Xi Dao",
            enName:"Guernsey",
            group:"",
            RE:/(\+|(00))?((44\-)|(44))\d[\d\-]+\d/
        },

        {
            value:"GN",
            phonePrefix:"224",
            cnName:"几内亚",
            cnPinyin:"Ji Nei Ya",
            enName:"Guinea",
            group:"",
            RE:/(\+|(00))?((224\-)|(224))\d[\d\-]+\d/
        },

        {
            value:"GW",
            phonePrefix:"245",
            cnName:"几内亚比绍",
            cnPinyin:"Ji Nei Ya Bi Shao",
            enName:"Guinea-Bissau",
            group:"",
            RE:/(\+|(00))?((245\-)|(245))\d[\d\-]+\d/
        },

        {
            value:"GY",
            phonePrefix:"592",
            cnName:"圭亚那",
            cnPinyin:"Gui Ya Nei",
            enName:"Guyana",
            group:"",
            RE:/(\+|(00))?((592\-)|(592))\d[\d\-]+\d/
        },

        {
            value:"HT",
            phonePrefix:"509",
            cnName:"海地",
            cnPinyin:"Hai Di",
            enName:"Haiti",
            group:"",
            RE:/(\+|(00))?((509\-)|(509))\d[\d\-]+\d/
        },

        {
            value:"HM",
            phonePrefix:"61",
            cnName:"赫德岛和麦克唐纳岛",
            cnPinyin:"He De Dao He Mai Ke Tang Na Dao",
            enName:"Heard Island and McDonald Islands",
            group:"",
            RE:/(\+|(00))?((61\-)|(61))\d[\d\-]+\d/
        },

        {
            value:"VA",
            phonePrefix:"39066",
            cnName:"梵蒂冈",
            cnPinyin:"Fan Di Gang",
            enName:"Holy See (the) [Vatican City State]",
            group:"",
            RE:/(\+|(00))?((39066\-)|(39066))\d[\d\-]+\d/
        },

        {
            value:"HN",
            phonePrefix:"504",
            cnName:"洪都拉斯",
            cnPinyin:"Hong Dou La Si",
            enName:"Honduras",
            group:"",
            RE:/(\+|(00))?((504\-)|(504))\d[\d\-]+\d/
        },

        {
            value:"HU",
            phonePrefix:"36",
            cnName:"匈牙利",
            cnPinyin:"Xiong Ya Li",
            enName:"Hungary",
            group:"",
            RE:/(\+|(00))?((36\-)|(36))\d[\d\-]+\d/
        },

        {
            value:"IS",
            phonePrefix:"354",
            cnName:"冰岛",
            cnPinyin:"Bing Dao",
            enName:"Iceland",
            group:"",
            RE:/(\+|(00))?((354\-)|(354))\d[\d\-]+\d/
        },

        {
            value:"IN",
            phonePrefix:"91",
            cnName:"印度",
            cnPinyin:"Yin Du",
            enName:"India",
            group:"",
            RE:/(\+|(00))?((91\-)|(91))\d[\d\-]+\d/
        },

        {
            value:"ID",
            phonePrefix:"62",
            cnName:"印度尼西亚",
            cnPinyin:"Yin Du Ni Xi Ya",
            enName:"Indonesia",
            group:"",
            RE:/(\+|(00))?((62\-)|(62))\d[\d\-]+\d/
        },

        {
            value:"IR",
            phonePrefix:"98",
            cnName:"伊朗",
            cnPinyin:"Yi Lang",
            enName:"Iran (the Islamic Republic of)",
            group:"",
            RE:/(\+|(00))?((98\-)|(98))\d[\d\-]+\d/
        },

        {
            value:"IQ",
            phonePrefix:"964",
            cnName:"伊拉克",
            cnPinyin:"Yi La Ke",
            enName:"Iraq",
            group:"",
            RE:/(\+|(00))?((964\-)|(964))\d[\d\-]+\d/
        },

        {
            value:"IE",
            phonePrefix:"353",
            cnName:"爱尔兰",
            cnPinyin:"Ai Er Lan",
            enName:"Ireland",
            group:"",
            RE:/(\+|(00))?((353\-)|(353))\d[\d\-]+\d/
        },

        {
            value:"IM",
            phonePrefix:"44",
            cnName:"英国属地曼岛",
            cnPinyin:"Ying Guo Shu Di Man Dao",
            enName:"Isle of Man",
            group:"",
            RE:/(\+|(00))?((44\-)|(44))\d[\d\-]+\d/
        },

        {
            value:"IL",
            phonePrefix:"972",
            cnName:"以色列",
            cnPinyin:"Yi Se Lie",
            enName:"Israel",
            group:"",
            RE:/(\+|(00))?((972\-)|(972))\d[\d\-]+\d/
        },

        {
            value:"IT",
            phonePrefix:"39",
            cnName:"意大利",
            cnPinyin:"Yi Da Li",
            enName:"Italy",
            group:"",
            RE:/(\+|(00))?((39\-)|(39))\d[\d\-]+\d/
        },

        {
            value:"JM",
            phonePrefix:"1876",
            cnName:"牙买加",
            cnPinyin:"Ya Mai Jia",
            enName:"Jamaica",
            group:"",
            RE:/(\+|(00))?((1876\-)|(1876))\d[\d\-]+\d/
        },

        {
            value:"JP",
            phonePrefix:"81",
            cnName:"日本",
            cnPinyin:"Ri Ben",
            enName:"Japan",
            group:"",
            RE:/(\+|(00))?((81\-)|(81))\d[\d\-]+\d/
        },

        {
            value:"JE",
            phonePrefix:"44",
            cnName:"泽西岛",
            cnPinyin:"Ze Xi Dao",
            enName:"Jersey",
            group:"",
            RE:/(\+|(00))?((44\-)|(44))\d[\d\-]+\d/
        },

        {
            value:"JO",
            phonePrefix:"962",
            cnName:"约旦",
            cnPinyin:"Yue Dan",
            enName:"Jordan",
            group:"",
            RE:/(\+|(00))?((962\-)|(962))\d[\d\-]+\d/
        },

        {
            value:"KZ",
            phonePrefix:"77",
            cnName:"哈萨克斯坦",
            cnPinyin:"Ha Sa Ke Si Tan",
            enName:"Kazakhstan",
            group:"",
            RE:/(\+|(00))?((77\-)|(77))\d[\d\-]+\d/
        },

        {
            value:"KE",
            phonePrefix:"254",
            cnName:"肯尼亚",
            cnPinyin:"Ken Ni Ya",
            enName:"Kenya",
            group:"",
            RE:/(\+|(00))?((254\-)|(254))\d[\d\-]+\d/
        },

        {
            value:"KI",
            phonePrefix:"686",
            cnName:"基里巴斯",
            cnPinyin:"Ji Li Ba Si",
            enName:"Kiribati",
            group:"",
            RE:/(\+|(00))?((686\-)|(686))\d[\d\-]+\d/
        },

        {
            value:"KP",
            phonePrefix:"850",
            cnName:"朝鲜",
            cnPinyin:"Chao Xian",
            enName:"Korea (the Democratic People's Republic of)",
            group:"",
            RE:/(\+|(00))?((850\-)|(850))\d[\d\-]+\d/
        },

        {
            value:"KR",
            phonePrefix:"82",
            cnName:"韩国",
            cnPinyin:"Han Guo",
            enName:"Korea (the Republic of)",
            group:"",
            RE:/(\+|(00))?((82\-)|(82))\d[\d\-]+\d/
        },

        {
            value:"KW",
            phonePrefix:"965",
            cnName:"科威特",
            cnPinyin:"Ke Wei Te",
            enName:"Kuwait",
            group:"",
            RE:/(\+|(00))?((965\-)|(965))\d[\d\-]+\d/
        },

        {
            value:"KG",
            phonePrefix:"996",
            cnName:"吉尔吉斯斯坦",
            cnPinyin:"Ji Er Ji Si Si Tan",
            enName:"Kyrgyzstan",
            group:"",
            RE:/(\+|(00))?((996\-)|(996))\d[\d\-]+\d/
        },

        {
            value:"LA",
            phonePrefix:"856",
            cnName:"老挝",
            cnPinyin:"Lao Wo",
            enName:"Lao People's Democratic Republic (the)",
            group:"",
            RE:/(\+|(00))?((856\-)|(856))\d[\d\-]+\d/
        },

        {
            value:"LV",
            phonePrefix:"371",
            cnName:"拉脱维亚",
            cnPinyin:"La Tuo Wei Ya",
            enName:"Latvia",
            group:"",
            RE:/(\+|(00))?((371\-)|(371))\d[\d\-]+\d/
        },

        {
            value:"LB",
            phonePrefix:"961",
            cnName:"黎巴嫩",
            cnPinyin:"Li Ba Nen",
            enName:"Lebanon",
            group:"",
            RE:/(\+|(00))?((961\-)|(961))\d[\d\-]+\d/
        },

        {
            value:"LS",
            phonePrefix:"266",
            cnName:"莱索托",
            cnPinyin:"Lai Suo Tuo",
            enName:"Lesotho",
            group:"",
            RE:/(\+|(00))?((266\-)|(266))\d[\d\-]+\d/
        },

        {
            value:"LR",
            phonePrefix:"231",
            cnName:"利比里亚",
            cnPinyin:"Li Bi Li Ya",
            enName:"Liberia",
            group:"",
            RE:/(\+|(00))?((231\-)|(231))\d[\d\-]+\d/
        },

        {
            value:"LY",
            phonePrefix:"218",
            cnName:"利比亚",
            cnPinyin:"Li Bi Ya",
            enName:"Libyan Arab Jamahiriya (the)",
            group:"",
            RE:/(\+|(00))?((218\-)|(218))\d[\d\-]+\d/
        },

        {
            value:"LI",
            phonePrefix:"423",
            cnName:"列支敦士登",
            cnPinyin:"Lie Zhi Dun Shi Deng",
            enName:"Liechtenstein",
            group:"",
            RE:/(\+|(00))?((423\-)|(423))\d[\d\-]+\d/
        },

        {
            value:"LT",
            phonePrefix:"370",
            cnName:"立陶宛",
            cnPinyin:"Li Tao Wan",
            enName:"Lithuania",
            group:"",
            RE:/(\+|(00))?((370\-)|(370))\d[\d\-]+\d/
        },

        {
            value:"LU",
            phonePrefix:"352",
            cnName:"卢森堡",
            cnPinyin:"Lu Sen Bao",
            enName:"Luxembourg",
            group:"",
            RE:/(\+|(00))?((352\-)|(352))\d[\d\-]+\d/
        },

        {
            value:"MK",
            phonePrefix:"389",
            cnName:"前南马其顿",
            cnPinyin:"Qian Nan Ma Qi Dun",
            enName:"Macedonia (the former Yugoslav Republic of)",
            group:"",
            RE:/(\+|(00))?((389\-)|(389))\d[\d\-]+\d/
        },

        {
            value:"MG",
            phonePrefix:"261",
            cnName:"马达加斯加",
            cnPinyin:"Ma Da Jia Si Jia",
            enName:"Madagascar",
            group:"",
            RE:/(\+|(00))?((261\-)|(261))\d[\d\-]+\d/
        },

        {
            value:"MW",
            phonePrefix:"265",
            cnName:"马拉维",
            cnPinyin:"Ma La Wei",
            enName:"Malawi",
            group:"",
            RE:/(\+|(00))?((265\-)|(265))\d[\d\-]+\d/
        },

        {
            value:"MY",
            phonePrefix:"60",
            cnName:"马来西亚",
            cnPinyin:"Ma Lai Xi Ya",
            enName:"Malaysia",
            group:"",
            RE:/(\+|(00))?((60\-)|(60))\d[\d\-]+\d/
        },

        {
            value:"MV",
            phonePrefix:"960",
            cnName:"马尔代夫",
            cnPinyin:"Ma Er Dai Fu",
            enName:"Maldives",
            group:"",
            RE:/(\+|(00))?((960\-)|(960))\d[\d\-]+\d/
        },

        {
            value:"ML",
            phonePrefix:"223",
            cnName:"马里",
            cnPinyin:"Ma Li",
            enName:"Mali",
            group:"",
            RE:/(\+|(00))?((223\-)|(223))\d[\d\-]+\d/
        },

        {
            value:"MT",
            phonePrefix:"356",
            cnName:"马耳他",
            cnPinyin:"Ma Er Ta",
            enName:"Malta",
            group:"",
            RE:/(\+|(00))?((356\-)|(356))\d[\d\-]+\d/
        },

        {
            value:"MH",
            phonePrefix:"692",
            cnName:"马绍尔群岛",
            cnPinyin:"Ma Shao Er Qun Dao",
            enName:"Marshall Islands (the)",
            group:"",
            RE:/(\+|(00))?((692\-)|(692))\d[\d\-]+\d/
        },

        {
            value:"MQ",
            phonePrefix:"596",
            cnName:"马提尼克",
            cnPinyin:"Ma Ti Ni Ke",
            enName:"Martinique",
            group:"",
            RE:/(\+|(00))?((596\-)|(596))\d[\d\-]+\d/
        },

        {
            value:"MR",
            phonePrefix:"222",
            cnName:"毛里塔尼亚",
            cnPinyin:"Mao Li Ta Ni Ya",
            enName:"Mauritania",
            group:"",
            RE:/(\+|(00))?((222\-)|(222))\d[\d\-]+\d/
        },

        {
            value:"MU",
            phonePrefix:"230",
            cnName:"毛里求斯",
            cnPinyin:"Mao Li Qiu Si",
            enName:"Mauritius",
            group:"",
            RE:/(\+|(00))?((230\-)|(230))\d[\d\-]+\d/
        },

        {
            value:"YT",
            phonePrefix:"262",
            cnName:"马约特",
            cnPinyin:"Ma Yue Te",
            enName:"Mayotte",
            group:"",
            RE:/(\+|(00))?((262\-)|(262))\d[\d\-]+\d/
        },

        {
            value:"MX",
            phonePrefix:"52",
            cnName:"墨西哥",
            cnPinyin:"Mo Xi Ge",
            enName:"Mexico",
            group:"",
            RE:/(\+|(00))?((52\-)|(52))\d[\d\-]+\d/
        },

        {
            value:"FM",
            phonePrefix:"691",
            cnName:"密克罗尼西亚联邦",
            cnPinyin:"Mi Ke Luo Ni Xi Ya Lian Bang",
            enName:"Micronesia (the Federated States of)",
            group:"",
            RE:/(\+|(00))?((691\-)|(691))\d[\d\-]+\d/
        },

        {
            value:"MD",
            phonePrefix:"373",
            cnName:"摩尔多瓦",
            cnPinyin:"Mo Er Duo Wa",
            enName:"Moldova (the Republic of)",
            group:"",
            RE:/(\+|(00))?((373\-)|(373))\d[\d\-]+\d/
        },

        {
            value:"MC",
            phonePrefix:"377",
            cnName:"摩纳哥",
            cnPinyin:"Mo Na Ge",
            enName:"Monaco",
            group:"",
            RE:/(\+|(00))?((377\-)|(377))\d[\d\-]+\d/
        },

        {
            value:"MN",
            phonePrefix:"976",
            cnName:"蒙古",
            cnPinyin:"Meng Gu",
            enName:"Mongolia",
            group:"",
            RE:/(\+|(00))?((976\-)|(976))\d[\d\-]+\d/
        },

        {
            value:"ME",
            phonePrefix:"382",
            cnName:"黑山",
            cnPinyin:"Hei Shan",
            enName:"Montenegro",
            group:"",
            RE:/(\+|(00))?((382\-)|(382))\d[\d\-]+\d/
        },

        {
            value:"MS",
            phonePrefix:"1664",
            cnName:"蒙特塞拉特",
            cnPinyin:"Meng Te Sai La Te",
            enName:"Montserrat",
            group:"",
            RE:/(\+|(00))?((1664\-)|(1664))\d[\d\-]+\d/
        },

        {
            value:"MA",
            phonePrefix:"212",
            cnName:"摩洛哥",
            cnPinyin:"Mo Luo Ge",
            enName:"Morocco",
            group:"",
            RE:/(\+|(00))?((212\-)|(212))\d[\d\-]+\d/
        },

        {
            value:"MZ",
            phonePrefix:"258",
            cnName:"莫桑比克",
            cnPinyin:"Mo Sang Bi Ke",
            enName:"Mozambique",
            group:"",
            RE:/(\+|(00))?((258\-)|(258))\d[\d\-]+\d/
        },

        {
            value:"MM",
            phonePrefix:"95",
            cnName:"缅甸",
            cnPinyin:"Mian Dian",
            enName:"Myanmar",
            group:"",
            RE:/(\+|(00))?((95\-)|(95))\d[\d\-]+\d/
        },

        {
            value:"NA",
            phonePrefix:"264",
            cnName:"纳米比亚",
            cnPinyin:"Na Mi Bi Ya",
            enName:"Namibia",
            group:"",
            RE:/(\+|(00))?((264\-)|(264))\d[\d\-]+\d/
        },

        {
            value:"NR",
            phonePrefix:"674",
            cnName:"瑙鲁",
            cnPinyin:"Nao Lu",
            enName:"Nauru",
            group:"",
            RE:/(\+|(00))?((674\-)|(674))\d[\d\-]+\d/
        },

        {
            value:"NP",
            phonePrefix:"977",
            cnName:"尼泊尔",
            cnPinyin:"Ni Bo Er",
            enName:"Nepal",
            group:"",
            RE:/(\+|(00))?((977\-)|(977))\d[\d\-]+\d/
        },

        {
            value:"NL",
            phonePrefix:"31",
            cnName:"荷兰",
            cnPinyin:"He Lan",
            enName:"Netherlands (the)",
            group:"",
            RE:/(\+|(00))?((31\-)|(31))\d[\d\-]+\d/
        },

        {
            value:"AN",
            phonePrefix:"599",
            cnName:"荷属安的列斯",
            cnPinyin:"He Shu An De Lie Si",
            enName:"Netherlands Antilles (the)",
            group:"",
            RE:/(\+|(00))?((599\-)|(599))\d[\d\-]+\d/
        },

        {
            value:"NC",
            phonePrefix:"687",
            cnName:"新喀里多尼亚",
            cnPinyin:"Xin Ka Li Duo Ni Ya",
            enName:"New Caledonia",
            group:"",
            RE:/(\+|(00))?((687\-)|(687))\d[\d\-]+\d/
        },

        {
            value:"NZ",
            phonePrefix:"64",
            cnName:"新西兰",
            cnPinyin:"Xin Xi Lan",
            enName:"New Zealand",
            group:"",
            RE:/(\+|(00))?((64\-)|(64))\d[\d\-]+\d/
        },

        {
            value:"NI",
            phonePrefix:"505",
            cnName:"尼加拉瓜",
            cnPinyin:"Ni Jia La Gua",
            enName:"Nicaragua",
            group:"",
            RE:/(\+|(00))?((505\-)|(505))\d[\d\-]+\d/
        },

        {
            value:"NE",
            phonePrefix:"227",
            cnName:"尼日尔",
            cnPinyin:"Ni Ri Er",
            enName:"Niger (the)",
            group:"",
            RE:/(\+|(00))?((227\-)|(227))\d[\d\-]+\d/
        },

        {
            value:"NG",
            phonePrefix:"234",
            cnName:"尼日利亚",
            cnPinyin:"Ni Ri Li Ya",
            enName:"Nigeria",
            group:"",
            RE:/(\+|(00))?((234\-)|(234))\d[\d\-]+\d/
        },

        {
            value:"NU",
            phonePrefix:"683",
            cnName:"纽埃",
            cnPinyin:"Niu Ai",
            enName:"Niue",
            group:"",
            RE:/(\+|(00))?((683\-)|(683))\d[\d\-]+\d/
        },

        {
            value:"NF",
            phonePrefix:"672",
            cnName:"诺福克岛",
            cnPinyin:"Nuo Fu Ke Dao",
            enName:"Norfolk Island",
            group:"",
            RE:/(\+|(00))?((672\-)|(672))\d[\d\-]+\d/
        },

        {
            value:"MP",
            phonePrefix:"1",
            cnName:"北马里亚纳",
            cnPinyin:"Bei Ma Li Ya Na",
            enName:"Northern Mariana Islands (the)",
            group:"",
            RE:/(\+|(00))?((1\-)|(1))\d[\d\-]+\d/
        },

        {
            value:"NO",
            phonePrefix:"47",
            cnName:"挪威",
            cnPinyin:"Nuo Wei",
            enName:"Norway",
            group:"",
            RE:/(\+|(00))?((47\-)|(47))\d[\d\-]+\d/
        },

        {
            value:"OM",
            phonePrefix:"968",
            cnName:"阿曼",
            cnPinyin:"A Man",
            enName:"Oman",
            group:"",
            RE:/(\+|(00))?((968\-)|(968))\d[\d\-]+\d/
        },

        {
            value:"PK",
            phonePrefix:"92",
            cnName:"巴基斯坦",
            cnPinyin:"Ba Ji Si Tan",
            enName:"Pakistan",
            group:"",
            RE:/(\+|(00))?((92\-)|(92))\d[\d\-]+\d/
        },

        {
            value:"PW",
            phonePrefix:"680",
            cnName:"帕劳",
            cnPinyin:"Pa Lao",
            enName:"Palau",
            group:"",
            RE:/(\+|(00))?((680\-)|(680))\d[\d\-]+\d/
        },

        {
            value:"PS",
            phonePrefix:"970",
            cnName:"巴勒斯坦",
            cnPinyin:"Ba Le Si Tan",
            enName:"Palestinian Territory (the Occupied)",
            group:"",
            RE:/(\+|(00))?((970\-)|(970))\d[\d\-]+\d/
        },

        {
            value:"PA",
            phonePrefix:"507",
            cnName:"巴拿马",
            cnPinyin:"Ba Na Ma",
            enName:"Panama",
            group:"",
            RE:/(\+|(00))?((507\-)|(507))\d[\d\-]+\d/
        },

        {
            value:"PG",
            phonePrefix:"675",
            cnName:"巴布亚新几内亚",
            cnPinyin:"Ba Bu Ya Xin Ji Nei Ya",
            enName:"Papua New Guinea",
            group:"",
            RE:/(\+|(00))?((675\-)|(675))\d[\d\-]+\d/
        },

        {
            value:"PY",
            phonePrefix:"595",
            cnName:"巴拉圭",
            cnPinyin:"Ba La Gui",
            enName:"Paraguay",
            group:"",
            RE:/(\+|(00))?((595\-)|(595))\d[\d\-]+\d/
        },

        {
            value:"PE",
            phonePrefix:"51",
            cnName:"秘鲁",
            cnPinyin:"Mi Lu",
            enName:"Peru",
            group:"",
            RE:/(\+|(00))?((51\-)|(51))\d[\d\-]+\d/
        },

        {
            value:"PH",
            phonePrefix:"63",
            cnName:"菲律宾",
            cnPinyin:"Fei Lu: Bin",
            enName:"Philippines (the)",
            group:"",
            RE:/(\+|(00))?((63\-)|(63))\d[\d\-]+\d/
        },

        {
            value:"PN",
            phonePrefix:"64",
            cnName:"皮特凯恩",
            cnPinyin:"Pi Te Kai En",
            enName:"Pitcairn",
            group:"",
            RE:/(\+|(00))?((64\-)|(64))\d[\d\-]+\d/
        },

        {
            value:"PL",
            phonePrefix:"48",
            cnName:"波兰",
            cnPinyin:"Bo Lan",
            enName:"Poland",
            group:"",
            RE:/(\+|(00))?((48\-)|(48))\d[\d\-]+\d/
        },

        {
            value:"PT",
            phonePrefix:"351",
            cnName:"葡萄牙",
            cnPinyin:"Pu Tao Ya",
            enName:"Portugal",
            group:"",
            RE:/(\+|(00))?((351\-)|(351))\d[\d\-]+\d/
        },

        {
            value:"PR",
            phonePrefix:"1",
            cnName:"波多黎各",
            cnPinyin:"Bo Duo Li Ge",
            enName:"Puerto Rico",
            group:"",
            RE:/(\+|(00))?((1\-)|(1))\d[\d\-]+\d/
        },

        {
            value:"QA",
            phonePrefix:"974",
            cnName:"卡塔尔",
            cnPinyin:"Ka Ta Er",
            enName:"Qatar",
            group:"",
            RE:/(\+|(00))?((974\-)|(974))\d[\d\-]+\d/
        },

        {
            value:"RE",
            phonePrefix:"262",
            cnName:"留尼汪",
            cnPinyin:"Liu Ni Wang",
            enName:"Réunion",
            group:"",
            RE:/(\+|(00))?((262\-)|(262))\d[\d\-]+\d/
        },

        {
            value:"RO",
            phonePrefix:"40",
            cnName:"罗马尼亚",
            cnPinyin:"Luo Ma Ni Ya",
            enName:"Romania",
            group:"",
            RE:/(\+|(00))?((40\-)|(40))\d[\d\-]+\d/
        },

        {
            value:"RU",
            phonePrefix:"7",
            cnName:"俄罗斯联邦",
            cnPinyin:"E Luo Si Lian Bang",
            enName:"Russian Federation (the)",
            group:"",
            RE:/(\+|(00))?((7\-)|(7))\d[\d\-]+\d/
        },

        {
            value:"RW",
            phonePrefix:"250",
            cnName:"卢旺达",
            cnPinyin:"Lu Wang Da",
            enName:"Rwanda",
            group:"",
            RE:/(\+|(00))?((250\-)|(250))\d[\d\-]+\d/
        },

        {
            value:"SH",
            phonePrefix:"290",
            cnName:"圣赫勒拿",
            cnPinyin:"Sheng He Le Na",
            enName:"Saint Helena",
            group:"",
            RE:/(\+|(00))?((290\-)|(290))\d[\d\-]+\d/
        },

        {
            value:"KN",
            phonePrefix:"1869",
            cnName:"圣基茨和尼维斯",
            cnPinyin:"Sheng Ji Ci He Ni Wei Si",
            enName:"Saint Kitts and Nevis",
            group:"",
            RE:/(\+|(00))?((1869\-)|(1869))\d[\d\-]+\d/
        },

        {
            value:"LC",
            phonePrefix:"1758",
            cnName:"圣卢西亚",
            cnPinyin:"Sheng Lu Xi Ya",
            enName:"Saint Lucia",
            group:"",
            RE:/(\+|(00))?((1758\-)|(1758))\d[\d\-]+\d/
        },

        {
            value:"PM",
            phonePrefix:"508",
            cnName:"圣皮埃尔和密克隆",
            cnPinyin:"Sheng Pi Ai Er He Mi Ke Long",
            enName:"Saint Pierre and Miquelon",
            group:"",
            RE:/(\+|(00))?((508\-)|(508))\d[\d\-]+\d/
        },

        {
            value:"VC",
            phonePrefix:"1784",
            cnName:"圣文森特和格林纳丁斯",
            cnPinyin:"Sheng Wen Sen Te He Ge Lin Na Ding Si",
            enName:"Saint Vincent and the Grenadines",
            group:"",
            RE:/(\+|(00))?((1784\-)|(1784))\d[\d\-]+\d/
        },

        {
            value:"WS",
            phonePrefix:"685",
            cnName:"萨摩亚",
            cnPinyin:"Sa Mo Ya",
            enName:"Samoa",
            group:"",
            RE:/(\+|(00))?((685\-)|(685))\d[\d\-]+\d/
        },

        {
            value:"SM",
            phonePrefix:"378",
            cnName:"圣马力诺",
            cnPinyin:"Sheng Ma Li Nuo",
            enName:"San Marino",
            group:"",
            RE:/(\+|(00))?((378\-)|(378))\d[\d\-]+\d/
        },

        {
            value:"ST",
            phonePrefix:"239",
            cnName:"圣多美和普林西比",
            cnPinyin:"Sheng Duo Mei He Pu Lin Xi Bi",
            enName:"Sao Tome and Principe",
            group:"",
            RE:/(\+|(00))?((239\-)|(239))\d[\d\-]+\d/
        },

        {
            value:"SA",
            phonePrefix:"966",
            cnName:"沙特阿拉伯",
            cnPinyin:"Sha Te A La Bo",
            enName:"Saudi Arabia",
            group:"",
            RE:/(\+|(00))?((966\-)|(966))\d[\d\-]+\d/
        },

        {
            value:"SN",
            phonePrefix:"221",
            cnName:"塞内加尔",
            cnPinyin:"Sai Nei Jia Er",
            enName:"Senegal",
            group:"",
            RE:/(\+|(00))?((221\-)|(221))\d[\d\-]+\d/
        },

        {
            value:"RS",
            phonePrefix:"381",
            cnName:"塞尔维亚",
            cnPinyin:"Sai Er Wei Ya",
            enName:"Serbia",
            group:"",
            RE:/(\+|(00))?((381\-)|(381))\d[\d\-]+\d/
        },

        {
            value:"SC",
            phonePrefix:"248",
            cnName:"塞舌尔",
            cnPinyin:"Sai She Er",
            enName:"Seychelles",
            group:"",
            RE:/(\+|(00))?((248\-)|(248))\d[\d\-]+\d/
        },

        {
            value:"SL",
            phonePrefix:"232",
            cnName:"塞拉利昂",
            cnPinyin:"Sai La Li Ang",
            enName:"Sierra Leone",
            group:"",
            RE:/(\+|(00))?((232\-)|(232))\d[\d\-]+\d/
        },

        {
            value:"SG",
            phonePrefix:"65",
            cnName:"新加坡",
            cnPinyin:"Xin Jia Po",
            enName:"Singapore",
            group:"",
            RE:/(\+|(00))?((65\-)|(65))\d[\d\-]+\d/
        },

        {
            value:"SK",
            phonePrefix:"421",
            cnName:"斯洛伐克",
            cnPinyin:"Si Luo Fa Ke",
            enName:"Slovakia",
            group:"",
            RE:/(\+|(00))?((421\-)|(421))\d[\d\-]+\d/
        },

        {
            value:"SI",
            phonePrefix:"386",
            cnName:"斯洛文尼亚",
            cnPinyin:"Si Luo Wen Ni Ya",
            enName:"Slovenia",
            group:"",
            RE:/(\+|(00))?((386\-)|(386))\d[\d\-]+\d/
        },

        {
            value:"SB",
            phonePrefix:"677",
            cnName:"所罗门群岛",
            cnPinyin:"Suo Luo Men Qun Dao",
            enName:"Solomon Islands (the)",
            group:"",
            RE:/(\+|(00))?((677\-)|(677))\d[\d\-]+\d/
        },

        {
            value:"SO",
            phonePrefix:"252",
            cnName:"索马里",
            cnPinyin:"Suo Ma Li",
            enName:"Somalia",
            group:"",
            RE:/(\+|(00))?((252\-)|(252))\d[\d\-]+\d/
        },

        {
            value:"ZA",
            phonePrefix:"27",
            cnName:"南非",
            cnPinyin:"Nan Fei",
            enName:"South Africa",
            group:"",
            RE:/(\+|(00))?((27\-)|(27))\d[\d\-]+\d/
        },

        {
            value:"ES",
            phonePrefix:"34",
            cnName:"西班牙",
            cnPinyin:"Xi Ban Ya",
            enName:"Spain",
            group:"",
            RE:/(\+|(00))?((34\-)|(34))\d[\d\-]+\d/
        },

        {
            value:"LK",
            phonePrefix:"94",
            cnName:"斯里兰卡",
            cnPinyin:"Si Li Lan Ka",
            enName:"Sri Lanka",
            group:"",
            RE:/(\+|(00))?((94\-)|(94))\d[\d\-]+\d/
        },

        {
            value:"SD",
            phonePrefix:"249",
            cnName:"苏丹",
            cnPinyin:"Su Dan",
            enName:"Sudan (the)",
            group:"",
            RE:/(\+|(00))?((249\-)|(249))\d[\d\-]+\d/
        },

        {
            value:"SR",
            phonePrefix:"597",
            cnName:"苏里南",
            cnPinyin:"Su Li Nan",
            enName:"Suriname",
            group:"",
            RE:/(\+|(00))?((597\-)|(597))\d[\d\-]+\d/
        },

        {
            value:"SJ",
            phonePrefix:"47",
            cnName:"斯瓦尔巴岛和扬马延岛",
            cnPinyin:"Si Wa Er Ba Dao He Yang Ma Yan Dao",
            enName:"Svalbard and Jan Mayen",
            group:"",
            RE:/(\+|(00))?((47\-)|(47))\d[\d\-]+\d/
        },

        {
            value:"SZ",
            phonePrefix:"268",
            cnName:"斯威士兰",
            cnPinyin:"Si Wei Shi Lan",
            enName:"Swaziland",
            group:"",
            RE:/(\+|(00))?((268\-)|(268))\d[\d\-]+\d/
        },

        {
            value:"SE",
            phonePrefix:"46",
            cnName:"瑞典",
            cnPinyin:"Rui Dian",
            enName:"Sweden",
            group:"",
            RE:/(\+|(00))?((46\-)|(46))\d[\d\-]+\d/
        },

        {
            value:"CH",
            phonePrefix:"41",
            cnName:"瑞士",
            cnPinyin:"Rui Shi",
            enName:"Switzerland",
            group:"",
            RE:/(\+|(00))?((41\-)|(41))\d[\d\-]+\d/
        },

        {
            value:"SY",
            phonePrefix:"963",
            cnName:"叙利亚",
            cnPinyin:"Xu Li Ya",
            enName:"Syrian Arab Republic (the)",
            group:"",
            RE:/(\+|(00))?((963\-)|(963))\d[\d\-]+\d/
        },

        {
            value:"TJ",
            phonePrefix:"992",
            cnName:"塔吉克斯坦",
            cnPinyin:"Ta Ji Ke Si Tan",
            enName:"Tajikistan",
            group:"",
            RE:/(\+|(00))?((992\-)|(992))\d[\d\-]+\d/
        },

        {
            value:"TZ",
            phonePrefix:"255",
            cnName:"坦桑尼亚",
            cnPinyin:"Tan Sang Ni Ya",
            enName:"Tanzania,United Republic of",
            group:"",
            RE:/(\+|(00))?((255\-)|(255))\d[\d\-]+\d/
        },

        {
            value:"TH",
            phonePrefix:"66",
            cnName:"泰国",
            cnPinyin:"Tai Guo",
            enName:"Thailand",
            group:"",
            RE:/(\+|(00))?((66\-)|(66))\d[\d\-]+\d/
        },

        {
            value:"TL",
            phonePrefix:"670",
            cnName:"东帝汶",
            cnPinyin:"Dong Di Wen",
            enName:"Timor-Leste",
            group:"",
            RE:/(\+|(00))?((670\-)|(670))\d[\d\-]+\d/
        },

        {
            value:"TG",
            phonePrefix:"228",
            cnName:"多哥",
            cnPinyin:"Duo Ge",
            enName:"Togo",
            group:"",
            RE:/(\+|(00))?((228\-)|(228))\d[\d\-]+\d/
        },

        {
            value:"TK",
            phonePrefix:"690",
            cnName:"托克劳",
            cnPinyin:"Tuo Ke Lao",
            enName:"Tokelau",
            group:"",
            RE:/(\+|(00))?((690\-)|(690))\d[\d\-]+\d/
        },

        {
            value:"TO",
            phonePrefix:"676",
            cnName:"汤加",
            cnPinyin:"Tang Jia",
            enName:"Tonga",
            group:"",
            RE:/(\+|(00))?((676\-)|(676))\d[\d\-]+\d/
        },

        {
            value:"TT",
            phonePrefix:"1868",
            cnName:"特立尼达和多巴哥",
            cnPinyin:"Te Li Ni Da He Duo Ba Ge",
            enName:"Trinidad and Tobago",
            group:"",
            RE:/(\+|(00))?((1868\-)|(1868))\d[\d\-]+\d/
        },

        {
            value:"TN",
            phonePrefix:"216",
            cnName:"突尼斯",
            cnPinyin:"Tu Ni Si",
            enName:"Tunisia",
            group:"",
            RE:/(\+|(00))?((216\-)|(216))\d[\d\-]+\d/
        },

        {
            value:"TR",
            phonePrefix:"90",
            cnName:"土耳其",
            cnPinyin:"Tu Er Qi",
            enName:"Turkey",
            group:"",
            RE:/(\+|(00))?((90\-)|(90))\d[\d\-]+\d/
        },

        {
            value:"TM",
            phonePrefix:"993",
            cnName:"土库曼斯坦",
            cnPinyin:"Tu Ku Man Si Tan",
            enName:"Turkmenistan",
            group:"",
            RE:/(\+|(00))?((993\-)|(993))\d[\d\-]+\d/
        },

        {
            value:"TC",
            phonePrefix:"1649",
            cnName:"特克斯和凯科斯群岛",
            cnPinyin:"Te Ke Si He Kai Ke Si Qun Dao",
            enName:"Turks and Caicos Islands (the)",
            group:"",
            RE:/(\+|(00))?((1649\-)|(1649))\d[\d\-]+\d/
        },

        {
            value:"TV",
            phonePrefix:"688",
            cnName:"图瓦卢",
            cnPinyin:"Tu Wa Lu",
            enName:"Tuvalu",
            group:"",
            RE:/(\+|(00))?((688\-)|(688))\d[\d\-]+\d/
        },

        {
            value:"UG",
            phonePrefix:"256",
            cnName:"乌干达",
            cnPinyin:"Wu Gan Da",
            enName:"Uganda",
            group:"",
            RE:/(\+|(00))?((256\-)|(256))\d[\d\-]+\d/
        },

        {
            value:"UA",
            phonePrefix:"380",
            cnName:"乌克兰",
            cnPinyin:"Wu Ke Lan",
            enName:"Ukraine",
            group:"",
            RE:/(\+|(00))?((380\-)|(380))\d[\d\-]+\d/
        },

        {
            value:"AE",
            phonePrefix:"971",
            cnName:"阿联酋",
            cnPinyin:"A Lian Qiu",
            enName:"United Arab Emirates (the)",
            group:"",
            RE:/(\+|(00))?((971\-)|(971))\d[\d\-]+\d/
        },

        {
            value:"GB",
            phonePrefix:"44",
            cnName:"英国",
            cnPinyin:"Ying Guo",
            enName:"United Kingdom (the)",
            group:"",
            RE:/(\+|(00))?((44\-)|(44))\d[\d\-]+\d/
        },

        {
            value:"US",
            phonePrefix:"1",
            cnName:"美国",
            cnPinyin:"Mei Guo",
            enName:"United States (the)",
            group:"",
            RE:/(\+|(00))?((1\-)|(1))\d[\d\-]+\d/
        },

        {
            value:"UM",
            phonePrefix:"1",
            cnName:"美国本土外小岛屿",
            cnPinyin:"Mei Guo Ben Tu Wai Xiao Dao Yu",
            enName:"United States Minor Outlying Islands (the)",
            group:"",
            RE:/(\+|(00))?((1\-)|(1))\d[\d\-]+\d/
        },

        {
            value:"UY",
            phonePrefix:"598",
            cnName:"乌拉圭",
            cnPinyin:"Wu La Gui",
            enName:"Uruguay",
            group:"",
            RE:/(\+|(00))?((598\-)|(598))\d[\d\-]+\d/
        },

        {
            value:"UZ",
            phonePrefix:"998",
            cnName:"乌兹别克斯坦",
            cnPinyin:"Wu Zi Bie Ke Si Tan",
            enName:"Uzbekistan",
            group:"",
            RE:/(\+|(00))?((998\-)|(998))\d[\d\-]+\d/
        },

        {
            value:"VU",
            phonePrefix:"678",
            cnName:"瓦努阿图",
            cnPinyin:"Wa Nu A Tu",
            enName:"Vanuatu",
            group:"",
            RE:/(\+|(00))?((678\-)|(678))\d[\d\-]+\d/
        },

        {
            value:"VE",
            phonePrefix:"58",
            cnName:"委内瑞拉",
            cnPinyin:"Wei Nei Rui La",
            enName:"Venezuela",
            group:"",
            RE:/(\+|(00))?((58\-)|(58))\d[\d\-]+\d/
        },

        {
            value:"VN",
            phonePrefix:"84",
            cnName:"越南",
            cnPinyin:"Yue Nan",
            enName:"Viet Nam",
            group:"",
            RE:/(\+|(00))?((84\-)|(84))\d[\d\-]+\d/
        },

        {
            value:"VG",
            phonePrefix:"1284",
            cnName:"英属维尔京群岛",
            cnPinyin:"Ying Shu Wei Er Jing Qun Dao",
            enName:"Virgin Islands (British)",
            group:"",
            RE:/(\+|(00))?((1284\-)|(1284))\d[\d\-]+\d/
        },

        {
            value:"VI",
            phonePrefix:"1340",
            cnName:"美属维尔京群岛",
            cnPinyin:"Mei Shu Wei Er Jing Qun Dao",
            enName:"Virgin Islands (U.S.)",
            group:"",
            RE:/(\+|(00))?((1340\-)|(1340))\d[\d\-]+\d/
        },

        {
            value:"WF",
            phonePrefix:"681",
            cnName:"瓦利斯和富图纳",
            cnPinyin:"Wa Li Si He Fu Tu Na",
            enName:"Wallis and Futuna",
            group:"",
            RE:/(\+|(00))?((681\-)|(681))\d[\d\-]+\d/
        },

        {
            value:"EH",
            phonePrefix:"212",
            cnName:"西撒哈拉",
            cnPinyin:"Xi Sa Ha La",
            enName:"Western Sahara",
            group:"",
            RE:/(\+|(00))?((212\-)|(212))\d[\d\-]+\d/
        },

        {
            value:"YE",
            phonePrefix:"967",
            cnName:"也门",
            cnPinyin:"Ye Men",
            enName:"Yemen",
            group:"",
            RE:/(\+|(00))?((967\-)|(967))\d[\d\-]+\d/
        },

        {
            value:"YU",
            phonePrefix:"381",
            cnName:"南斯拉夫",
            cnPinyin:"Nan Si La Fu",
            enName:"Yugoslavia",
            group:"",
            RE:/(\+|(00))?((381\-)|(381))\d[\d\-]+\d/
        },

        {
            value:"ZM",
            phonePrefix:"260",
            cnName:"赞比亚",
            cnPinyin:"Zan Bi Ya",
            enName:"Zambia",
            group:"",
            RE:/(\+|(00))?((260\-)|(260))\d[\d\-]+\d/
        },

        {
            value:"ZW",
            phonePrefix:"263",
            cnName:"津巴布韦",
            cnPinyin:"Jin Ba Bu Wei",
            enName:"Zimbabwe",
            group:"",
            RE:/(\+|(00))?((263\-)|(263))\d[\d\-]+\d/
        }
    ],
    /**
     * 根据地区编码或手机区号查找地区
     * @param code 地区编码或手机区号
     * @returns {{phonePrefix: string, RE: RegExp, cnName: string, cnPinyin: string, enName: string, value: string, group: string}|null}
     */
    getArea:function(code){
        if(code.startsWith('+')) code=code.substring(1);
        for(let i=0; i<this.areas.length; i++){
            if(this.areas[i].value==code || this.areas[i].phonePrefix==code) return this.areas[i];
        }
        return null;
    },

    /**
     * 手机/电话号码详情[区号部分,不含区号部分]
     * @param num
     * @returns {[undefined, *]|[string, undefined]}
     */
    getPhoneNumberDetail:function(num){
        if(Str.isBlank(num)) return ["",num];

        if(num.startsWith("+")) num=num.substring(1);

        let detail=num.split("\-");
        if(detail.length==1) return ["",num];

        let c=this.getArea(detail[0]);
        if(c==null && detail[0].startsWith("00")) c=this.getArea(detail[0].substring(2));
        if(!c) return ["",num];

        return detail;
    },

    /**
     * 电话号码是否有效
     * @param numberPart1 完整电话号码或者电话区号
     * @param numberPart2 不含区号部分的电话号码
     * @returns {boolean}
     */
    isPhoneNumberValid:function(numberPart1, numberPart2){
        if(Str.isBlank(numberPart1)) return false;

        let detail=numberPart2 ? [numberPart1, numberPart2] : this.getPhoneNumberDetail(numberPart1);

        let c=this.getArea(detail[0]);
        if(c==null && detail[0].startsWith("00")) c=this.getArea(detail[0].substring(2));
        if(!c) return false;

        let valid=true;

        if(!(detail[0]+'-'+detail[1]).match(c.RE)) valid=false;
        if(!detail[1].match(/\d{6,20}/)) valid=false;//6~20个数字
        if(c.RE2 && !detail[1].match(c.RE2)) valid=false;//除国际区号外的部分不符合规则
        if(c.RE3 && detail.length>2 && !detail[2].match(c.RE3)) valid=false;//分机号不符合规则

        return valid;
    },

    /**
     * 格式化号码
     * @param phoneNumber
     */
    formatPhoneNumber:function(phoneNumber){
        let cells = this.getPhoneNumberDetail(phoneNumber);
        if(Str.isBlank(cells[0])) return "+"+Countries.DEFAULT_MOBILE_CODE+"-"+cells[1];
        else return (cells[0].startsWith("00")?"":"+")+cells[0]+"-"+cells[1];
    },

    /**
     *
     * @param v
     */
    clearPhoneNumber:function(v){
        if(Str.isBlank(v)) return v;
        v=Str.replaceAll(v, '--', '-');
        return v.replace(/[^0-9\-]/g, '');
    },

    //电话输入组件实例
    PhoneNumberComponents:[],

    /**
     *
     * @param id
     * @returns {*}
     */
    getComponent:function (id){
        return this.PhoneNumberComponents[id];
    },

    //电话组件区号改变时
    PhoneNumberComponentPrefixChanged:function(selectorId, value, text){
        let componentId=Str.replaceAll(selectorId, '_prefix_selector', '');
        if(Countries.PhoneNumberComponents[componentId]) Countries.PhoneNumberComponents[componentId].changed();
    },

    //电话组件号码改变时
    PhoneNumberComponentNumberChanged:function(componentId){
        if(Countries.PhoneNumberComponents[componentId]) Countries.PhoneNumberComponents[componentId].changed();
    }
}

/**
 *
 * @param id 组件id
 * @param container 容器
 * @param style 组件的css classname
 * @param phonePrefixStyle 电话区号的css classname
 * @param phoneNumberStyle 电话号码的css classname
 * @param width 组件宽度
 * @param prefixWidth 区号选择器宽度
 * @param numberWidth 号码输入框宽度
 * @param onchange 号码改变时回调
 * @param readOnly 是否只读
 * @param placeholder 号码输入框的placeholder
 * @constructor
 */
function PhoneNumberComponent(id, container, style, phonePrefixStyle, phoneNumberStyle, width, prefixWidth, numberWidth, onchange, readOnly, placeholder){
    this.id=id;
    this.container=(typeof container)=='string'?_$(container):container;
    this.style=Str.isBlank(style)?'PhoneNumberComponent':style;
    this.phonePrefixStyle=Str.isBlank(phonePrefixStyle)?'PhoneNumberPrefix':phonePrefixStyle;
    this.phoneNumberStyle=Str.isBlank(phoneNumberStyle)?'PhoneNumber':phoneNumberStyle;
    this.width=width;
    this.prefixWidth=prefixWidth;
    this.numberWidth=(typeof numberWidth)=='number' && numberWidth>0 ? numberWidth : (width - prefixWidth - 3);
    this.phonePrefixSelector=null;
    this.onchange=onchange;
    this.readOnly=(typeof readOnly)!='boolean'?false:readOnly;
    this.placeholder=placeholder?placeholder:'I{手机号码}';
    Countries.PhoneNumberComponents[this.id]=this;
    this.build();
}

PhoneNumberComponent.prototype.resize=function (width, numberWidth){
    this.width=width;
    this.numberWidth=(typeof numberWidth)=='number' && numberWidth>0 ? numberWidth : (width - this.prefixWidth - 3)
    _$(this.id).style.width=this.width+'px';
    _$(this.id+'_number').style.width=this.numberWidth+'px';
}

/**
 * 创建组件
 */
PhoneNumberComponent.prototype.build=function(){
    let _style='';
    let _prefixStyle='';
    let _numberStyle='';

    if(this.width) _style=' style="width: '+this.width+'px;"';
    if(this.prefixWidth) _prefixStyle=' style="width: '+this.prefixWidth+'px;"';
    if(this.numberWidth) _numberStyle=' style="width: '+this.numberWidth+'px;"';

    let htm=[];
    htm.push('<div id="'+this.id+'" class="'+this.style+'"'+_style+'>');
    htm.push('	<div id="'+this.id+'_prefix" class="'+this.phonePrefixStyle+'"'+_prefixStyle+'></div>');
    htm.push('	<div id="'+this.id+'_number" class="'+this.phoneNumberStyle+'"'+_numberStyle+'>');
    htm.push('  <input type="text" id="'+this.id+'_number_input" placeholder="'+this.placeholder+'" onkeyup="Countries.PhoneNumberComponentNumberChanged(\''+this.id+'\');"');
    if(this.readOnly) htm.push(' readonly="true" style="background-color:#eee;"');
    htm.push('  /></div>');
    htm.push('</div>');
    this.container.innerHTML=Lang.convert(htm.join(''));
    htm=null;
    delete htm;

    let areas=[];
    for(let i=0; i<Countries.areas.length; i++){
        let area=Countries.areas[i];
        areas.push([area.phonePrefix, '+'+area.phonePrefix+'('+(Lang.getCurrentLang().id=='cn'?area.cnName:area.enName)+')']);
    }

    this.phonePrefixSelector=new JSelector(this.id+'_prefix',
        this.id+'_prefix_selector',
        this.prefixWidth,
        null,
        0,
        200,
        areas,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Countries.PhoneNumberComponentPrefixChanged);
    this.phonePrefixSelector.readOnly=this.readOnly;
    this.phonePrefixSelector.build();
}

/**
 *
 */
PhoneNumberComponent.prototype.getValue=function(){
    return '+'+this.phonePrefixSelector.itemCurrent[0]+'-'+Countries.clearPhoneNumber(_$(this.id+'_number_input').value);
}

/**
 *
 * @param phoneNumber
 */
PhoneNumberComponent.prototype.setValue=function(phoneNumber){
    phoneNumber=Countries.formatPhoneNumber(phoneNumber);
    let detail=Countries.getPhoneNumberDetail(phoneNumber);
    let area=Countries.getArea(detail[0]);
    if(!area) return;

    this.phonePrefixSelector.setCurrent([area.phonePrefix, '+'+area.phonePrefix+'('+(Lang.getCurrentLang().id=='cn'?area.cnName:area.enName)+')'], true);
    _$(this.id+'_number_input').value=detail[1];
    Countries.PhoneNumberComponentNumberChanged(this.id);
}


/**
 *
 */
PhoneNumberComponent.prototype.changed=function(){
    if(this.onchange) this.onchange(this.id, this.phonePrefixSelector.itemCurrent[0], Countries.clearPhoneNumber(_$(this.id+'_number_input').value), this.getValue());
}