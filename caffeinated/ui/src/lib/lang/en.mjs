export default {
    'meta.name': 'English',
    'meta.code': 'en',
    'meta.flag': '🇬🇧',
    'meta.direction': 'ltr',
    'meta.translators': ' ',

    // {placeholder}: some text passed to the translator and is replaced
    // [placeholder]: grabs a translation key
    // %placeholder%: ui components passed to you, example usage can be found in the chatbot

    // All keys prefixed with `sr.` are for screen readers only.
    // They will not be shown to end-users.

    'generic.quote.left': "'",
    'generic.quote.right': "'",

    'app.love': 'Made with ♥ by Casterlabs',
    'app.copyright': 'Copyright © {year} Casterlabs. All rights reserved.',

    'modal.close': 'Close',
    'sr.modal.close': 'Close Modal',

    'sr.navigation.back': 'Go Back',

    show_me_how: 'Show me how',

    'page.dashboard': 'Dashboard',
    'page.chat': 'Chat',

    /* ---- Widgets & Alerts ---- */

    'page.widgets': 'Widgets & Alerts',
    'page.widgets.info': 'Add these to OBS as Browser sources.',
    'page.widgets.info.show_me_how.modal.title': 'How to create an OBS Browser Source',
    'sr.page.widgets.create': 'Create a new widget',
    'page.widgets.create.new': '(New)',
    'sr.page.widgets.delete': 'Delete',
    'sr.page.widgets.copy_link': 'Copy Link',
    'page.widgets.create.category.ALERTS': 'Alerts',
    'page.widgets.create.category.LABELS': 'Labels',
    'page.widgets.create.category.INTERACTION': 'Interaction',
    'page.widgets.create.category.GOALS': 'Goals',
    'page.widgets.create.category.OTHER': 'Other',

    'sr.page.widget.editor.edit_name': 'Edit name',

    /* ---- Docks ---- */

    'page.docks': 'Docks',
    'page.docks.info': 'Add these to OBS as Browser Docks.',
    'page.docks.info.show_me_how.modal.title': 'How to create an OBS Browser Dock',

    /* ---- Chat Bot ---- */

    'page.chat_bot': 'Chat Bot',
    'page.chat_bot.platform.ANY': 'Any Platform',

    'sr.page.chat_bot.remove': 'Remove',

    'page.chat_bot.commands': 'Commands',
    'page.chat_bot.commands.type.COMMAND': 'runs',
    'page.chat_bot.commands.type.CONTAINS': 'mentions',
    'page.chat_bot.commands.format.COMMAND': 'When someone from %platform% %action% <b>!</b>%action_target%, send: %message%',
    'page.chat_bot.commands.format.CONTAINS': 'When someone from %platform% %action% <b>[generic.quote.left]</b>%action_target%<b>[generic.quote.right]</b>, reply with: %message%',
    'page.chat_bot.commands.example': 'Checkout casterlabs.co!',
    'sr.page.chat_bot.commands.add': 'Create another command',

    'page.chat_bot.shouts': 'Shouts',
    'page.chat_bot.shouts.format': 'When someone from %platform% %action% say: %message%',
    'page.chat_bot.shouts.DONATION': 'Donates',
    'page.chat_bot.shouts.FOLLOW': 'Follows',
    'page.chat_bot.shouts.RAID': 'Raids',
    'page.chat_bot.shouts.SUBSCRIPTION': 'Subscribes',
    'page.chat_bot.shouts.example': 'Thank you for the follow!',

    'page.chat_bot.timers': 'Timers',
    'page.chat_bot.timers.format': 'Every %seconds% seconds, send one of the following:',
    'page.chat_bot.timers.example': 'I love Casterlabs!',

    'page.chat_bot.settings': 'Settings',
    'page.chat_bot.settings.sender': 'Send messages from:',
    'page.chat_bot.settings.sender.SYSTEM': 'the @Casterlabs account',
    'page.chat_bot.settings.sender.CLIENT': 'your account',
    'page.chat_bot.settings.hide_commands_from_chat': 'Hide commands from chat',
    'page.chat_bot.settings.hide_commands_from_chat.description': 'Makes Caffeinated hide all !commands and responses from the chat widget.',
    'page.chat_bot.settings.hide_timers_from_chat': 'Hide timers from chat',
    'page.chat_bot.settings.hide_timers_from_chat.description': 'Makes Caffeinated hide all timer messages from the chat widget.',
    'page.chat_bot.settings.hide_from_chatbots': 'Hide these accounts from chat',
    'page.chat_bot.settings.hide_from_chatbots.description': 'Hide some additional accounts from your chat widget, such as <pre>@Nightbot</pre>.',
    'sr.page.chat_bot.settings.hide_from_chatbots.add': 'Hide another account',

    /* ---- Settings ---- */

    'page.settings': 'Settings',

    'page.settings.appearance': 'Appearance',
    'page.settings.appearance.theme': 'Theme',
    'page.settings.appearance.theme.CASTERLABS_LIGHT': 'Light',
    'page.settings.appearance.theme.CASTERLABS_DARK': 'Dark',
    'page.settings.appearance.theme.SYSTEM': 'Follow System',
    'page.settings.appearance.icon': 'Icon',
    'page.settings.appearance.emojis': 'Emojis 😀',
    'page.settings.appearance.emojis.SYSTEM': 'System',
    'page.settings.appearance.language': 'Language',
    'page.settings.appearance.close_to_tray': 'Close button sends to tray',
    'page.settings.appearance.close_to_tray.description': 'Makes Caffeinated cozy up in the background when you close it.',
    'page.settings.appearance.mikeys_mode': "Mikey's Mode",
    'page.settings.appearance.mikeys_mode.description': '<a class="text-link" href="https://twitter.com/Casterlabs/status/1508475284944736268" target="_blank">For those of you who need more time to cook your pockets :^)</a>',

    'page.settings.plugins': 'Plugins',
    'page.settings.plugins.open_directory': 'Open Plugins Folder',
    'page.settings.plugins.internal_plugin': 'Internal Plugin',
    'page.settings.plugins.file.load': 'Load',
    'page.settings.plugins.file.unload': 'Unload',

    'page.settings.accounts': 'Accounts',
    'page.settings.accounts.music_services': 'Music Services',
    'page.settings.accounts.music_services.enable': 'Enable',

    'page.settings.about': 'About',
    'page.settings.about.enable_unsafe': 'Enable Stupidly Unsafe Settings'
};