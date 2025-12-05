Note: This mod is based on the excellent idea and concept of the [Create Shuffle Filter](https://modrinth.com/mod/create-shuffle-filter) mod by Agent772. The original mod was developed for Neoforge.

I am not a professional mod developer, but I really wanted this functionality for my own Fabric playthrough. Therefore, I took the time to re-implement the concept myself.

⚠️ Disclaimer on Updates
-------------------------
Please be aware that since I developed this primarily for my personal use, I may not be able to guarantee frequent updates or extensive support. Updates will likely only happen when I personally need them for a new version of Minecraft or Fabric.

# Create: Shuffle Fabric
The core idea I re-implemented is to embrace The Beauty of Uncertainty in your automated Create contraptions. This lightweight mod achieves that by introducing one new item: the Shuffle Filter.

🛠️ How it Works
----------------
When you place this filter into a Create Deployer on a contraption, it enables truly random block placement, allowing you to build with variety.

The filter supports two distinct modes:

Weighted Mode:
The probability of placing an item is based on its stack quantity inside the Deployer's inventory. The more items of a kind you have, the higher the chance it will be placed.

Equal Mode:
Every item in the inventory has an equal probability of being placed, regardless of quantity.

In any other context (like being used in mechanical crafters or funnels), the Shuffle Filter behaves just like a regular list filter. If the filter is set to Weighted Mode, it will respect NBT data. Or if the filter is set to Equal Mode, it will ignore NBT data.
