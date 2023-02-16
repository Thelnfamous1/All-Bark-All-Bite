package com.infamous.all_bark_all_bite.client.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.network.ABABNetwork;
import com.infamous.all_bark_all_bite.common.network.ServerboundAdjustInstrumentPacket;
import com.infamous.all_bark_all_bite.common.util.InstrumentUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class InstrumentAdjustmentScreen extends Screen {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, "textures/gui/instrument_adjustment.png");
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int INSTRUMENTS_COLUMNS = 1; // 4 for stonecutter buttons
    private static final int INSTRUMENTS_ROWS = 3;
    private static final int INSTRUMENTS_IMAGE_SIZE_WIDTH = 64; // 16 for stonecutter buttons
    private static final int INSTRUMENTS_IMAGE_SIZE_HEIGHT = 18;
    private static final int SCROLLER_FULL_HEIGHT = 54;
    private static final int INSTRUMENTS_X = 52;
    private static final int INSTRUMENTS_Y = 14;
    private static final int INSTRUMENT_NAME_COLOR = 0xCCCCCC;
    protected int imageWidth = 176;
    protected int imageHeight = 166;
    private final Player owner;
    private final ItemStack adjustableInstrument;
    private final InteractionHand hand;
    private final List<Holder<Instrument>> instruments;
    private int selectedInstrumentIndex = -1;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private int leftPos;
    private int topPos;

    public InstrumentAdjustmentScreen(Player owner, ItemStack adjustableInstrument, InteractionHand hand, Iterable<Holder<Instrument>> instruments) {
        super(GameNarrator.NO_TITLE);
        this.owner = owner;
        this.adjustableInstrument = adjustableInstrument;
        this.hand = hand;
        this.instruments = ImmutableList.copyOf(instruments);
        this.minecraft = Minecraft.getInstance(); // need to do this to prevent NPEs
    }

    @Override
    protected void init() {
        //super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public boolean keyPressed(int keysym, int scancode, int p_97767_) {
        InputConstants.Key key = InputConstants.getKey(keysym, scancode);
        if (super.keyPressed(keysym, scancode, p_97767_)) {
            return true;
        } else if (this.minecraft.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void render(PoseStack p_99337_, int p_99338_, int p_99339_, float p_99340_) {
        this.renderBg(p_99337_, p_99338_, p_99339_);
        this.renderTooltip(p_99337_, p_99338_, p_99339_);
        super.render(p_99337_, p_99338_, p_99339_, p_99340_);
    }

    protected void renderBg(PoseStack poseStack, int p_99330_, int p_99331_) {
        this.renderBackground(poseStack);
        this.setFocused(null);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(poseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        int k = (int)(41.0F * this.scrollOffs);
        this.blit(poseStack, i + 119, j + SCROLLER_HEIGHT + k, this.imageWidth + (this.isScrollBarActive() ? 0 : SCROLLER_WIDTH), 0, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        int instrumentsLeftPos = this.leftPos + INSTRUMENTS_X;
        int instrumentsTopPos = this.topPos + INSTRUMENTS_Y;
        int j1 = this.startIndex + SCROLLER_WIDTH;
        this.renderButtons(poseStack, p_99330_, p_99331_, instrumentsLeftPos, instrumentsTopPos, j1);
        this.renderInstruments(poseStack, instrumentsLeftPos, instrumentsTopPos, j1);
    }

    protected void renderTooltip(PoseStack p_99333_, int p_99334_, int p_99335_) {
        //super.renderTooltip(p_99333_, p_99334_, p_99335_);
        int i = this.leftPos + INSTRUMENTS_X;
        int j = this.topPos + INSTRUMENTS_Y;
        int k = this.startIndex + SCROLLER_WIDTH;

        for(int l = this.startIndex; l < k && l < this.instruments.size(); ++l) {
            int i1 = l - this.startIndex;
            int j1 = i + i1 % INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_WIDTH;
            int k1 = j + i1 / INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_HEIGHT + 2;
            if (p_99334_ >= j1 && p_99334_ < j1 + INSTRUMENTS_IMAGE_SIZE_WIDTH && p_99335_ >= k1 && p_99335_ < k1 + INSTRUMENTS_IMAGE_SIZE_HEIGHT) {
                this.renderTooltip(p_99333_, getInstrumentTooltipLines(this.instruments.get(l)), Optional.empty(), p_99334_, p_99335_);
            }
        }
    }

    private static List<Component> getInstrumentTooltipLines(Holder<Instrument> instrument) {
        List<Component> tooltipLines = Lists.newArrayList();
        MutableComponent instrumentTooltip = InstrumentUtil.getInstrumentTooltip(instrument);
        tooltipLines.add(instrumentTooltip.withStyle(ChatFormatting.WHITE));
        return tooltipLines;
    }

    private void renderButtons(PoseStack poseStack, int p_99343_, int p_99344_, int instrumentsLeftPos, int instrumentsTopPos, int p_99347_) {
        for(int i = this.startIndex; i < p_99347_ && i < this.instruments.size(); ++i) {
            int j = i - this.startIndex;
            int k = instrumentsLeftPos + j % INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_WIDTH;
            int l = j / INSTRUMENTS_COLUMNS;
            int i1 = instrumentsTopPos + l * INSTRUMENTS_IMAGE_SIZE_HEIGHT + 2;
            int j1 = this.imageHeight;
            if (i == this.selectedInstrumentIndex) {
                j1 += INSTRUMENTS_IMAGE_SIZE_HEIGHT;
            } else if (p_99343_ >= k && p_99344_ >= i1 && p_99343_ < k + INSTRUMENTS_IMAGE_SIZE_WIDTH && p_99344_ < i1 + INSTRUMENTS_IMAGE_SIZE_HEIGHT) {
                j1 += INSTRUMENTS_IMAGE_SIZE_HEIGHT * 2;
            }

            this.blit(poseStack, k, i1 - 1, 0, j1, INSTRUMENTS_IMAGE_SIZE_WIDTH, INSTRUMENTS_IMAGE_SIZE_HEIGHT);
        }

    }

    private void renderInstruments(PoseStack poseStack, int instrumentsLeftPos, int instrumentsTopPos, int p_99351_) {
        for(int i = this.startIndex; i < p_99351_ && i < this.instruments.size(); ++i) {
            int j = i - this.startIndex;
            int k = instrumentsLeftPos + j % INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_WIDTH;
            int l = j / INSTRUMENTS_COLUMNS;
            int i1 = instrumentsTopPos + l * INSTRUMENTS_IMAGE_SIZE_HEIGHT + 2;

            this.font.draw(poseStack, InstrumentUtil.getInstrumentTooltip(this.instruments.get(i)), (float)k, (float)i1, INSTRUMENT_NAME_COLOR);
            //this.minecraft.getItemRenderer().renderAndDecorateItem(this.instruments.get(i).getResultItem(), k, i1);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int p_99320_) {
        this.scrolling = false;
        int instrumentsLeftPos = this.leftPos + INSTRUMENTS_X;
        int instrumentsTopPos = this.topPos + INSTRUMENTS_Y;
        int width = this.startIndex + SCROLLER_WIDTH;

        for(int l = this.startIndex; l < width; ++l) {
            int i1 = l - this.startIndex;
            double adjustedClickX = x - (double)(instrumentsLeftPos + i1 % INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_WIDTH);
            double adjustedClickY = y - (double)(instrumentsTopPos + i1 / INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_HEIGHT);
            if (adjustedClickX >= 0.0D && adjustedClickY >= 0.0D && adjustedClickX < INSTRUMENTS_IMAGE_SIZE_WIDTH && adjustedClickY < INSTRUMENTS_IMAGE_SIZE_HEIGHT && this.clickMenuButton(l)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_POWER_SELECT, 1.0F));
                this.updateInstrument(this.instruments.get(this.selectedInstrumentIndex));
                //this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, l);
                return true;
            }
        }

        instrumentsLeftPos = this.leftPos + 119;
        instrumentsTopPos = this.topPos + 9;
        if (x >= (double)instrumentsLeftPos && x < (double)(instrumentsLeftPos + SCROLLER_WIDTH) && y >= (double)instrumentsTopPos && y < (double)(instrumentsTopPos + SCROLLER_FULL_HEIGHT)) {
            this.scrolling = true;
        }

        return super.mouseClicked(x, y, p_99320_);
    }

    private boolean clickMenuButton(int index) {
        if (this.isValidInstrumentIndex(index)) {
            this.selectedInstrumentIndex = index;
            //this.setupResultSlot();
        }

        return true;
    }

    private boolean isValidInstrumentIndex(int index) {
        return index >= 0 && index < this.instruments.size();
    }

    @Override
    public boolean mouseDragged(double p_99322_, double p_99323_, int p_99324_, double p_99325_, double p_99326_) {
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + INSTRUMENTS_Y;
            int j = i + SCROLLER_FULL_HEIGHT;
            this.scrollOffs = ((float)p_99323_ - (float)i - (SCROLLER_HEIGHT / 2.0F)) / ((float)(j - i) - SCROLLER_HEIGHT);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5D) * INSTRUMENTS_COLUMNS;
            return true;
        } else {
            return super.mouseDragged(p_99322_, p_99323_, p_99324_, p_99325_, p_99326_);
        }
    }

    @Override
    public boolean mouseScrolled(double p_99314_, double p_99315_, double p_99316_) {
        if (this.isScrollBarActive()) {
            int offscreenRows = this.getOffscreenRows();
            float f = (float)p_99316_ / (float)offscreenRows;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)offscreenRows) + 0.5D) * INSTRUMENTS_COLUMNS;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.instruments.size() > (INSTRUMENTS_COLUMNS * INSTRUMENTS_ROWS);
    }

    protected int getOffscreenRows() {
        return (this.instruments.size() + INSTRUMENTS_COLUMNS - 1) / Math.max(1, INSTRUMENTS_COLUMNS - INSTRUMENTS_ROWS);
    }

    private void updateInstrument(Holder<Instrument> instrument) {
        InstrumentUtil.setSoundVariantId(this.adjustableInstrument, instrument);
        int slot = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().selected : Inventory.SLOT_OFFHAND;
        ABABNetwork.INSTANCE.sendToServer(new ServerboundAdjustInstrumentPacket(slot, InstrumentUtil.getInstrumentLocation(instrument)));
    }

}
