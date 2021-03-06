package me.iacn.biliroaming.hook

import me.iacn.biliroaming.BiliBiliPackage.Companion.instance
import me.iacn.biliroaming.utils.*
import java.lang.reflect.Type

class JsonHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    companion object {
        val bottomItems = mutableListOf<BottomItem>()
        val drawerItems = mutableListOf<BottomItem>()
    }

    override fun startHook() {
        Log.d("startHook: Json")

        val tabResponseClass =
            "tv.danmaku.bili.ui.main2.resource.MainResourceManager\$TabResponse".findClassOrNull(
                mClassLoader
            )
        val accountMineClass =
            "tv.danmaku.bili.ui.main2.api.AccountMine".findClassOrNull(mClassLoader)
        val splashClass = "tv.danmaku.bili.ui.splash.SplashData".findClassOrNull(mClassLoader)
        val tabClass =
            "tv.danmaku.bili.ui.main2.resource.MainResourceManager\$Tab".findClassOrNull(
                mClassLoader
            )
        val defaultWordClass =
            "tv.danmaku.bili.ui.main2.api.SearchDefaultWord".findClassOrNull(mClassLoader)
        val defaultKeywordClass =
            "com.bilibili.search.api.DefaultKeyword".findClassOrNull(mClassLoader)
        val brandSplashDataClass =
            "tv.danmaku.bili.ui.splash.brand.BrandSplashData".findClassOrNull(mClassLoader)
        val eventEntranceClass =
            "tv.danmaku.bili.ui.main.event.model.EventEntranceModel".findClassOrNull(mClassLoader)
        val searchRanksClass = "com.bilibili.search.api.SearchRanks".findClassOrNull(mClassLoader)
        val searchReferralClass =
            "com.bilibili.search.api.SearchReferral".findClassOrNull(mClassLoader)
        val followingcardSearchRanksClass =
            "com.bilibili.bplus.followingcard.net.entity.b".findClassOrNull(mClassLoader)
        val spaceClass =
            "com.bilibili.app.authorspace.api.BiliSpace".findClassOrNull(mClassLoader)
        val ogvApiResponseClass =
            "tv.danmaku.bili.ui.offline.api.OgvApiResponse".findClassOrNull(mClassLoader)

        instance.fastJsonClass?.hookAfterMethod(
            instance.fastJsonParse(),
            String::class.java,
            Type::class.java,
            Int::class.javaPrimitiveType,
            "com.alibaba.fastjson.parser.Feature[]"
        ) { param ->
            var result = param.result ?: return@hookAfterMethod
            if (result.javaClass == instance.generalResponseClass) {
                result = result.getObjectField("data") ?: return@hookAfterMethod
            }

            when (result.javaClass) {
                tabResponseClass -> {
                    val data = result.getObjectField("tabData")

                    bottomItems.clear()
                    val hides = sPrefs.getStringSet("hided_bottom_items", mutableSetOf())!!
                    data?.getObjectFieldAs<MutableList<*>?>("bottom")?.removeAll {
                        val uri = it?.getObjectFieldAs<String>("uri")
                        val id = it?.getObjectFieldAs<String>("tabId")
                        val showing = id !in hides
                        bottomItems.add(
                            BottomItem(
                                it?.getObjectFieldAs("name"),
                                uri, id, showing
                            )
                        )
                        showing.not()
                    }

                    if (sPrefs.getBoolean("drawer", false) && !sPrefs.getBoolean("hidden", false)) {
                        data?.getObjectFieldAs<MutableList<*>?>("bottom")?.removeAll {
                            it?.getObjectFieldAs<String?>("uri")
                                ?.startsWith("bilibili://user_center/mine")
                                ?: false
                        }
                    }

                    // ???????????????????????????/?????????????????????
                    if (sPrefs.getBoolean("add_bangumi", false)) {
                        val tab = data?.getObjectFieldAs<MutableList<Any>>("tab")
                        val hasBangumiCN = tab?.fold(false) { acc, it ->
                            val uri = it.getObjectFieldAs<String>("uri")
                            acc || uri.endsWith("bilibili://pgc/home")
                        }
                        val hasBangumiTW = tab?.fold(false) { acc, it ->
                            val uri = it.getObjectFieldAs<String>("uri")
                            acc || uri.startsWith("bilibili://following/home_activity_tab/6544")
                        }
                        // ????????????????????????
                        if (hasBangumiCN != null && !hasBangumiCN) {
                            val bangumiCN = tabClass?.new()
                                ?.setObjectField("tabId", "50")
                                ?.setObjectField("name", "??????????????????")
                                ?.setObjectField("uri", "bilibili://pgc/home")
                                ?.setObjectField("reportId", "bangumi")
                                ?.setIntField("pos", 50)
                            bangumiCN?.let { l ->
                                tab.forEach {
                                    it.setIntField("pos", it.getIntField("pos") + 0)
                                }
                                tab.add(0, l)
                            }
                        }
                        // ???????????????????????????
                        if (hasBangumiTW != null && !hasBangumiTW) {
                            val bangumiTW = tabClass?.new()
                                ?.setObjectField("tabId", "60")
                                ?.setObjectField("name", "?????????????????????")
                                ?.setObjectField(
                                    "uri",
                                    "bilibili://following/home_activity_tab/6544"
                                )
                                ?.setObjectField("reportId", "bangumi")
                                ?.setIntField("pos", 60)
                            bangumiTW?.let { l ->
                                tab.forEach {
                                    it.setIntField("pos", it.getIntField("pos") + 0)
                                }
                                tab.add(0, l)
                            }
                        }
                    }

                    // ???????????????????????????/?????????????????????
                    if (sPrefs.getBoolean("add_movie", false)) {
                        val tab = data?.getObjectFieldAs<MutableList<Any>>("tab")
                        val hasMovieCN = tab?.fold(false) { acc, it ->
                            val uri = it.getObjectFieldAs<String>("uri")
                            acc || uri.startsWith("bilibili://pgc/home?home_flow_type=2")
                        }
                        val hasMovieTW = tab?.fold(false) { acc, it ->
                            val uri = it.getObjectFieldAs<String>("uri")
                            acc || uri.startsWith("bilibili://following/home_activity_tab/168644")
                        }
                        // ????????????????????????
                        if (hasMovieCN != null && !hasMovieCN) {
                            val movieCN = tabClass?.new()
                                ?.setObjectField("tabId", "70")
                                ?.setObjectField("name", "??????????????????")
                                ?.setObjectField("uri", "bilibili://pgc/home?home_flow_type=2")
                                ?.setObjectField("reportId", "film")
                                ?.setIntField("pos", 70)
                            movieCN?.let { l ->
                                tab.forEach {
                                    it.setIntField("pos", it.getIntField("pos") + 0)
                                }
                                tab.add(0, l)
                            }
                        }
                        // ???????????????????????????
                        if (hasMovieTW != null && !hasMovieTW) {
                            // ????????????????????????
                            if (hasMovieCN != null && !hasMovieCN && platform != "android_b") {
                                val movieTW = tabClass?.new()
                                    ?.setObjectField("tabId", "40")
                                    ?.setObjectField("name", "??????")
                                    ?.setObjectField(
                                        "uri",
                                        "bilibili://following/home_activity_tab/168644"
                                    )
                                    ?.setObjectField("reportId", "jptv")
                                    ?.setIntField("pos", 40)
                                movieTW?.let { l ->
                                    tab.forEach {
                                        it.setIntField("pos", it.getIntField("pos") + 0)
                                    }
                                    tab.add(0, l)
                                }
                            } else {
                                val movieTW = tabClass?.new()
                                    ?.setObjectField("tabId", "80")
                                    ?.setObjectField("name", "?????????????????????")
                                    ?.setObjectField(
                                        "uri",
                                        "bilibili://following/home_activity_tab/168644"
                                    )
                                    ?.setObjectField("reportId", "jptv")
                                    ?.setIntField("pos", 80)
                                movieTW?.let { l ->
                                    tab.forEach {
                                        it.setIntField("pos", it.getIntField("pos") + 0)
                                    }
                                    tab.add(0, l)
                                }
                            }
                        }
                    }

                    if (sPrefs.getStringSet("customize_home_tab", emptySet())
                            ?.isNotEmpty() == true
                    ) {
                        val tab = data?.getObjectFieldAs<MutableList<Any>>("tab")
                        val purifytabset =
                            sPrefs.getStringSet("customize_home_tab", emptySet()).orEmpty()
                        tab?.removeAll {
                            it.getObjectFieldAs<String>("uri").run {
                                when {
                                    this == "bilibili://live/home" -> purifytabset.contains("live")
                                    this == "bilibili://pegasus/promo" -> purifytabset.contains("promo")
                                    this == "bilibili://pegasus/hottopic" -> purifytabset.contains("hottopic")
                                    this == "bilibili://pgc/home" || this == "bilibili://following/home_activity_tab/6544" -> purifytabset.contains(
                                        "bangumi"
                                    )
                                    this == "bilibili://pgc/home?home_flow_type=2" || this == "bilibili://following/home_activity_tab/168644" -> purifytabset.contains(
                                        "movie"
                                    )
                                    startsWith("bilibili://pegasus/op/") || startsWith("bilibili://following/home_activity_tab") -> purifytabset.contains(
                                        "activity"
                                    )
                                    else -> purifytabset.contains("other_tabs")
                                }
                            }
                        }
                    }

                    if (sPrefs.getBoolean("purify_game", false) &&
                        sPrefs.getBoolean("hidden", false)
                    ) {
                        val top = data?.getObjectFieldAs<MutableList<*>?>("top")
                        top?.removeAll {
                            val uri = it?.getObjectFieldAs<String?>("uri")
                            uri?.startsWith("bilibili://game_center/home") ?: false
                        }
                    }

                }
                accountMineClass -> {
                    drawerItems.clear()
                    val hides = sPrefs.getStringSet("hided_drawer_items", mutableSetOf())!!
                    var deleteUpper = false
                    if (("????????????" !in hides && "????????????" in hides && "????????????" in hides) || 
                        ("????????????" !in hides && "????????????" in hides && "????????????" in hides)) {
                        deleteUpper = true
                        Log.toast("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", true)
                    }
                    if (platform == "android_hd") {
                        result.getObjectFieldOrNullAs<MutableList<*>?>("padSectionList")?.removeAll { items ->
                            // ????????????
                            val title = items?.getObjectFieldAs<String>("title")
                            val uri = items?.getObjectFieldAs<String>("uri")
                            val id = items?.getObjectField("id").toString()

                            // ????????????????????????
                            if (sPrefs.getBoolean("add_custom_button", false) && id == sPrefs.getString("custom_button_id", "")){
                                val icon = items?.getObjectFieldAs<String>("icon").toString()
                                items?.setObjectField("title", sPrefs.getString("custom_button_title", title))
                                    ?.setObjectField("uri", sPrefs.getString("custom_button_uri", uri))
                                    ?.setObjectField("icon", sPrefs.getString("custom_button_icon", icon))
                                return@removeAll false
                            }

                            val showing = id !in hides
                            // ??????????????? drawerItems
                            drawerItems.add(BottomItem(title, uri, id, showing))
                            // ????????????
                            if (sPrefs.getBoolean("purify_drawer_reddot", false)) items?.setIntField("redDot",0)
                            showing.not()
                        }
                    } else {
                        result.getObjectFieldOrNullAs<MutableList<*>?>("sectionListV2")?.forEach { sections ->
                            try {
                                // ??????????????? drawerItems
                                val bigTitle = sections?.getObjectFieldOrNull("title").toString()
                                if (bigTitle != "null") drawerItems.add(BottomItem("??????????????????", null, bigTitle, bigTitle !in hides))
                                // ????????????
                                sections?.getObjectFieldOrNullAs<MutableList<*>?>("itemList")
                                    ?.removeAll { items ->
                                        // ????????????
                                        val title = try {
                                            items?.getObjectFieldAs<String>("title")
                                        } catch (thr: Throwable) {
                                            return@removeAll false
                                        }
                                        if (title == "null") return@removeAll false
                                        val uri = items?.getObjectFieldAs<String>("uri")
                                        val id = items?.getObjectFieldAs<Int>("id").toString()

                                        // ????????????????????????
                                        if (sPrefs.getBoolean("add_custom_button", false) && id == sPrefs.getString("custom_button_id", "")){
                                            val icon = items?.getObjectFieldAs<String>("icon").toString()
                                            items?.setObjectField("title", sPrefs.getString("custom_button_title", title))
                                                ?.setObjectField("uri", sPrefs.getString("custom_button_uri", uri))
                                                ?.setObjectField("icon", sPrefs.getString("custom_button_icon", icon))
                                            return@removeAll false
                                        }

                                        val showing = id !in hides
                                        // ??????????????? drawerItems
                                        drawerItems.add(BottomItem(title, uri, id, showing))
                                        // ????????????
                                        if (sPrefs.getBoolean("purify_drawer_reddot", false)) items?.setIntField("redDot", 0)
                                        showing.not()
                                    }
                                // ????????????
                                val button = sections?.getObjectFieldOrNull("button")
                                if (button != null) {
                                    val buttonText = button.getObjectField("text").toString()
                                    val showing = buttonText !in hides
                                    if (buttonText != "null") {
                                        val uri = button.getObjectFieldAs<String>("jumpUrl")
                                        drawerItems.add(BottomItem("?????????", uri, buttonText, showing))
                                        if (!showing) sections.setObjectField("button", null)
                                    }
                                }
                                // ????????????
                                if (sPrefs.getBoolean("drawer_style_switch", false)) {
                                    sections?.setIntField(
                                        "style",
                                        when {
                                            sPrefs.getBoolean("drawer_style", false) -> 2
                                            else -> 1
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                Log.d(e)
                            }
                        }
                        // ???????????????
                        if (!deleteUpper) {
                            result.getObjectFieldOrNullAs<MutableList<*>?>("sectionListV2")?.removeAll { sections ->
                                sections?.getObjectFieldOrNull("title").toString() in hides
                            }
                        }
                    }
                    accountMineClass.findFieldOrNull("vipSectionRight")?.set(result, null)
                    if (sPrefs.getBoolean("custom_theme", false)) {
                        result.setObjectField("garbEntrance", null)
                    }
                }
                splashClass -> if (sPrefs.getBoolean("purify_splash", false) &&
                    sPrefs.getBoolean("hidden", false)
                ) {
                    result.getObjectFieldAs<MutableList<*>?>("splashList")?.clear()
                    result.getObjectFieldAs<MutableList<*>?>("strategyList")?.clear()
                }
                defaultWordClass, defaultKeywordClass, searchRanksClass, searchReferralClass, followingcardSearchRanksClass -> if (sPrefs.getBoolean(
                        "purify_search",
                        false
                    ) &&
                    sPrefs.getBoolean("hidden", false)
                ) {
                    result.javaClass.fields.forEach {
                        if (it.type != Int::class.javaPrimitiveType)
                            result.setObjectField(it.name, null)
                    }
                }
                brandSplashDataClass -> if (sPrefs.getBoolean("custom_splash", false) ||
                    sPrefs.getBoolean("custom_splash_logo", false)
                ) {
                    val brandList = result.getObjectFieldAs<MutableList<Any>>("brandList")
                    val showList = result.getObjectFieldAs<MutableList<Any>>("showList")
                    brandList.clear()
                    showList.clear()
                }
                eventEntranceClass -> if (sPrefs.getBoolean("purify_game", false) &&
                    sPrefs.getBoolean("hidden", false)
                ) {
                    result.setObjectField("online", null)
                    result.setObjectField("hash", "")
                }
                spaceClass -> {
                    val purifySpaceSet =
                        sPrefs.getStringSet("customize_space", emptySet()).orEmpty()
                    if (purifySpaceSet.isNotEmpty()) {
                        purifySpaceSet.forEach {
                            if (!it.contains(".")) result.setObjectField(
                                it,
                                null
                            )
                        }
                        // Exceptions (adV2 -> ad + adV2)
                        if (purifySpaceSet.contains("adV2")) result.setObjectField("ad", null)

                        result.getObjectFieldAs<MutableList<*>?>("tab")?.removeAll {
                            when (it?.getObjectFieldAs<String?>("param")) {
                                "home" -> purifySpaceSet.contains("tab.home")
                                "dynamic" -> purifySpaceSet.contains("tab.dynamic")
                                "contribute" -> purifySpaceSet.contains("tab.contribute")
                                "shop" -> purifySpaceSet.contains("tab.shop")
                                "bangumi" -> purifySpaceSet.contains("tab.bangumi")
                                "cheese" -> purifySpaceSet.contains("tab.cheese")
                                else -> false
                            }
                        }
                    }
                }

                ogvApiResponseClass -> if (sPrefs.getBoolean("allow_download", false)) {
                    val resultObj = result.getObjectFieldAs<ArrayList<Any>>("result")
                    for (i in resultObj) {
                        i.setIntField("isPlayable", 1)
                    }
                }
            }
        }

        val searchRankClass = "com.bilibili.search.api.SearchRank".findClass(mClassLoader)
        val searchGuessClass =
            "com.bilibili.search.api.SearchReferral\$Guess".findClass(mClassLoader)
        val categoryClass = "tv.danmaku.bili.category.CategoryMeta".findClass(mClassLoader)

        instance.fastJsonClass?.hookAfterMethod(
            "parseArray",
            String::class.java,
            Class::class.java
        ) { param ->
            @Suppress("UNCHECKED_CAST")
            val result = param.result as? MutableList<Any>
            when (param.args[1] as Class<*>) {
                searchRankClass, searchGuessClass ->
                    if (sPrefs.getBoolean("purify_search", false) && sPrefs.getBoolean(
                            "hidden",
                            false
                        )
                    ) {
                        result?.clear()
                    }
                categoryClass ->
                    if (sPrefs.getBoolean("music_notification", false)) {
                        val hasMusic = result?.fold(false) { r, i ->
                            r || (i.getObjectFieldAs<String?>("mUri")
                                ?.startsWith("bilibili://music")
                                ?: false)
                        } ?: false
                        if (!hasMusic) {
                            result?.add(
                                categoryClass.new()
                                    .setObjectField("mTypeName", "??????")
                                    .setObjectField(
                                        "mCoverUrl",
                                        "http://i0.hdslb.com/bfs/archive/85d6dddbdc9746fed91c65c2c3eb3a0a453eadaf.png"
                                    )
                                    .setObjectField("mUri", "bilibili://music/home?from=category")
                                    .setIntField("mType", 1)
                                    .setIntField("mParentTid", 0)
                                    .setIntField("mTid", 65543)
                            )
                        }
                    }
            }
        }

        if (sPrefs.getBoolean("purify_city", false) &&
            sPrefs.getBoolean("hidden", false)
        ) {
            listOf(
                "com.bapis.bilibili.app.dynamic.v1.DynTabReply",
                "com.bapis.bilibili.app.dynamic.v2.DynTabReply"
            ).forEach { clazz ->
                clazz.hookAfterMethod(
                    mClassLoader,
                    "getDynTabList"
                ) { param ->
                    param.result = (param.result as List<*>).filter {
                        it?.callMethodAs<Long>("getCityId") == 0L
                    }
                }
            }
        }
    }

    data class BottomItem(
        val name: String?,
        val uri: String?,
        val id: String?,
        var showing: Boolean
    )
}
