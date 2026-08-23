package com.person98.imevoquest.util;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

public class ConfigurationHelper {
   private File file;
   private FileConfiguration fileConfiguration;
   String fileName;

   public ConfigurationHelper(Plugin plugin, String fileName) {
      this(plugin, null, fileName, null);
   }

   public ConfigurationHelper(Plugin plugin, String folder, String fileName) {
      this(plugin, folder, fileName, null);
   }

   public ConfigurationHelper(Plugin plugin, String folder, String fileName, String defaults) {
      this.fileName = fileName;
      if (!plugin.getDataFolder().exists()) {
         plugin.getDataFolder().mkdirs();
      }

      if (!fileName.isEmpty()) {
         fileName = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
      }

      new File(plugin.getDataFolder() + (folder == null ? "" : File.separator + folder)).mkdirs();
      if (folder == null) {
         this.file = new File(plugin.getDataFolder(), fileName.isEmpty() ? "config.yml" : fileName);
      } else {
         this.file = new File(plugin.getDataFolder(), fileName.isEmpty() ? "config.yml" : folder + File.separator + fileName);
      }

      try {
         if (!this.file.exists()) {
            if (folder == null) {
               plugin.saveResource(fileName, false);
            } else if (!this.file.exists()) {
               this.createNewFile();
            }
         }

         this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
         this.fileConfiguration.loadFromString(Files.toString(this.file, StandardCharsets.UTF_8));
         if (defaults != null && !defaults.isEmpty()) {
            Reader defaultConfigStream = new InputStreamReader(Objects.requireNonNull(plugin.getResource(defaults)), "UTF-8");
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
            this.fileConfiguration.setDefaults(defaultConfig);
            this.fileConfiguration.options().copyDefaults(true);
            this.save();
         }
      } catch (InvalidConfigurationException | IOException exception) {
         exception.printStackTrace();
      }
   }

   public void createNewFile() throws IOException {
      this.file.createNewFile();
   }

   public Object get(String path, Object defaultValue) {
      Object object = this.fileConfiguration.get(path);
      return object == null ? defaultValue : object;
   }

   public Object get(String path) {
      return this.get(path, null);
   }

   public String getString(String path) {
      return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.fileConfiguration.getString(path)));
   }

   public String getString(String path, String defaulty) {
      return ChatColor.translateAlternateColorCodes('&', this.fileConfiguration.getString(path, defaulty));
   }

   public int getInt(String path) {
      return (Integer)this.get(path, 0);
   }

   public List<?> getList(String path) {
      return (List<?>)this.get(path, Lists.newArrayList());
   }

   public List<String> getStringList(String path) {
      List<String> list = Lists.newArrayList();

      for (Object value : (List<?>)this.get(path, Lists.newArrayList())) {
         list.add(ChatColor.translateAlternateColorCodes('&', String.valueOf(value)));
      }

      return list;
   }

   public EntityType getEntityType(String path) {
      return EntityType.valueOf(this.getString(path));
   }

   public double getDouble(String path) {
      return (Double)this.get(path, 0.0);
   }

   public float getFloat(String path) {
      return (Float)this.get(path, 0.0);
   }

   public List<Integer> getIntegetList(String path) {
      return (List<Integer>)this.get(path, Lists.newArrayList());
   }

   public List<Double> getDoubleList(String path) {
      return (List<Double>)this.get(path, Lists.newArrayList());
   }

   public List<Float> getFloatList(String path) {
      return (List<Float>)this.get(path, Lists.newArrayList());
   }

   public boolean getBoolean(String path) {
      return (Boolean)this.get(path, false);
   }

   public void set(String path, Object value) {
      this.fileConfiguration.set(path, value);
   }

   public ConfigurationSection getConfigurationSection(String path) {
      return this.fileConfiguration.getConfigurationSection(path);
   }

   public boolean contains(String path) {
      return this.get(path) != null;
   }

   public void setLocation(String path, Location location) {
      if (location == null) {
         this.set(path, null);
      } else {
         String locationString = location.getWorld().getName()
            + ";"
            + location.getX()
            + ";"
            + location.getY()
            + ";"
            + location.getZ()
            + ";"
            + location.getYaw()
            + ";"
            + location.getPitch();
         this.set(path, locationString);
      }
   }

   public Location getLocation(String path) {
      String locationString = (String)this.get(path, null);
      if (locationString == null) {
         return null;
      } else {
         String[] locationSplit = locationString.split(";");
         String world = locationSplit[0];
         double x = Double.parseDouble(locationSplit[1]);
         double y = Double.parseDouble(locationSplit[2]);
         double z = Double.parseDouble(locationSplit[3]);
         float yaw = Float.parseFloat(locationSplit[4]);
         float pitch = Float.parseFloat(locationSplit[5]);
         return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
      }
   }

   public void reload() {
      this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
      this.save();
   }

   public void save() {
      try {
         this.fileConfiguration.save(this.file);
      } catch (IOException exception) {
         exception.printStackTrace();
      }
   }
}
