package Astrellipse.Bank;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bank implements Listener, CommandExecutor {
    //dbインスタンス
    DbBank db;
    //通帳アイテム
    public static ItemStack passBook;
    //ATMインベントリ
    Inventory atmenu;
    //ATMenuの項目名一覧
    List<String> menu = new ArrayList<>();
    //bankは利用可能か
    public static boolean bank = false;

    JavaPlugin plugin;
    public Bank(JavaPlugin plugin,DbBank db) {
        this.plugin = plugin;

        //イベントリスナ
        Bukkit.getPluginManager().registerEvents(this,this.plugin);
        //通帳アイテム作成
        createPBook();
        ///ATMenuの項目作成
        menu = Arrays.asList("おかねを預ける","おかねを引き出す","送金");
        //ATMenu作成
        createATM();
        //db
        this.db = db;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ///ATMコマンド
        if (command.getName().equals("atm")) {
            if (!bank) {
                sender.sendMessage("現在利用できません");
                return true;
            }
            Player player = (Player) sender;
            if (args.length == 0) {
                //TAMenuを開く
                player.openInventory(atmenu);
                return true;
            }
            //args[0]がgetの場合通帳を渡す
            if (args[0].equals("get")) {
                addItem(player,passBook);
                sender.sendMessage("通帳を獲得しました");
                return true;
            }


            //args[0]がw
            if (args[0].equals("w")) {
                //argsの数が2
                if (args.length != 2) {
                    sender.sendMessage("引数のミスの可能性があります");
                    return true;
                }
                //args[1]が数字か
                int money;
                try {
                    money = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("数字を入力する場所が間違っているようです");
                    return true;
                }
                //その人の在庫の数を確認
                if (db.dget(sender.getName()) < money) {
                    sender.sendMessage("在庫が足りません");
                    return true;
                }
                //在庫>=args[1]なら引き出して持たせる
                db.drem(sender.getName(),money);
                ItemStack item = new ItemStack(Material.EMERALD);
                item.setAmount(money);
                addItem((Player)sender,item);
                sender.sendMessage(money+"個引き出しました");
                return true;
            }
            //args[0]がp
            if (args[0].equals("p")) {
                if (args.length != 3) {
                    sender.sendMessage("引数のミスの可能性があります");
                    return true;
                }
                //args[2]が数字か
                int money;
                try {
                    money = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("数字を入力する場所が間違っているようです");
                    return true;
                }
                //その人の在庫の数を確認
                if (db.dget(sender.getName()) < money) {
                    sender.sendMessage("在庫が足りません");
                    return true;
                }
                //在庫>=args[1]なら引いてargs[1]の在庫に追加
                String name = args[1];
                db.drem(sender.getName(),money);
                db.dadd(name,money);
                sender.sendMessage(name+"に"+money+"個送りました");
                return true;
            }
            //args[0]がv
            if (args[0].equals("v")) {
                sender.sendMessage("あなたの所持金: "+db.dget(sender.getName()));
                return true;
            }
        }
        return true;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        //右クリック
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            //通帳か
            if (e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getEnchantLevel(Enchantment.LOYALTY) == passBook.getItemMeta().getEnchantLevel(Enchantment.LOYALTY)) {
                if (!bank) {
                    e.getPlayer().sendMessage("現在利用できません");
                    return;
                }
                //ATMenuインベントリを開く
                e.getPlayer().openInventory(atmenu);
            }
        }
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent e) {
        //ATMメニューのインベントリクリック時の操作
        if (e.getView().getTitle().equals("§a§lATMenu§r")) {
            e.setCancelled(true);
            //ガラス板クリック&無のクリック
            try {
                if (e.getCurrentItem().getItemMeta().getDisplayName().equals("\s")) {
                    return;
                }
            } catch (NullPointerException exception) {
                return;
            }
            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("おかねを預ける")) {
                e.getWhoClicked().openInventory(Bukkit.createInventory(null, 9 * 6, "おかねを預ける"));
            } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("おかねを引き出す")) {
                TextComponent w = new net.md_5.bungee.api.chat.TextComponent("§o§nおかねを引き出す§r ←クリック");
                w.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/atm w "));
                e.getWhoClicked().spigot().sendMessage(w);
                e.getWhoClicked().closeInventory();
            } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("送金")) {
                TextComponent p = new net.md_5.bungee.api.chat.TextComponent("§o§n送金§r ←クリック");
                p.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/atm p "));
                e.getWhoClicked().spigot().sendMessage(p);
                e.getWhoClicked().closeInventory();
            }
        }
        //預けるインベントリ開いているとき
        if (e.getView().getTitle().equals("おかねを預ける")) {
            //数字キーキャンセル
            if (e.getClick().isKeyboardClick()) {
                e.setCancelled(true);
                return;
            }
            try {
                //hold時してるのがエメラルドか無であるか
                if (isEme(e.getCursor())  || e.getCursor().getType() == Material.AIR) {
                    //上記でクリックした場所が下記ならキャンセル
                    if (!isEme(e.getCurrentItem())) {

                        e.setCancelled(true);
                    } else if (e.getCursor().getType() != Material.AIR) {
                        e.setCancelled(true);
                    }
                } else {
                    e.setCancelled(true);
                }
            } catch (Exception exception) {
            }

        }
    }



    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        //おかねを預けるインベントリを閉じたとき
        if (e.getView().getTitle().equals("おかねを預ける")) {
            int amo = 0;
            for (ItemStack item :e.getInventory().getContents()) {
                try {
                    amo += item.getAmount();
                } catch (NullPointerException exception){

                }

            }
            //在庫にamo個追加
            db.dadd(e.getPlayer().getName(),amo);
            e.getPlayer().sendMessage(amo+"個増えました");
            return;
        }
    }

    //通帳作成
    void createPBook() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§l通帳");
        meta.setLore(Arrays.asList("ろーぷろ銀行の通帳"));
        //火に投げても大丈夫…？
        meta.setFireResistant(true);
        //ツールチップを隠すは何も見れなくなってだめでした
//        meta.setHideTooltip(true);
        //不可壊
        meta.setUnbreakable(true);
        //ロイヤリティというエンチャントを100レベル付与 これで見分ける
        meta.addEnchant(Enchantment.LOYALTY,100,true);
        item.setItemMeta(meta);
        passBook = item;
    }

    //アイテム渡す
    void addItem(Player player,ItemStack item) {
        //空きがあるか無いかでそれぞれの渡し方
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItem(player.getLocation(),item);
        } else {
            player.getInventory().addItem(item);
        }
    }

    //ATMmenu作成
    void createATM() {
        Inventory inv = Bukkit.createInventory(null,9*6,"§a§lATMenu§r");
        ItemStack air = new ItemStack(Material.IRON_BARS);
        ItemMeta airmeta = air.getItemMeta();
        airmeta.setDisplayName("\s");
        airmeta.setHideTooltip(true);
        //hidetooltipここで使えるかも
        air.setItemMeta(airmeta);
        //ガラス板をとりあえず敷き詰める
        for (int i = 0; i<9*6; i++) {
            inv.setItem(i,air);
        }
        //預ける、引き出す、送金(今のところ)
        inv.setItem(9*3+2,createItem(Material.HOPPER, menu.get(0),Arrays.asList("")));
        inv.setItem(9*3+4,createItem(Material.DROPPER, menu.get(1),Arrays.asList("")));
        inv.setItem(9*3+6,createItem(Material.MINECART,menu.get(2),Arrays.asList("")));
        atmenu = inv;
    }

    ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (!lore.get(0).equals("")) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    //天然エメラルドか
    boolean isEme(ItemStack item) {
        if (item.hasItemMeta()) {
            return false;
        }
        if (item.getType() != Material.EMERALD) {
            return false;
        }
        return true;
    }
}
