package com.infamous.all_bark_all_bite.client.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.item.PetWhistleItem;
import com.infamous.all_bark_all_bite.common.network.ABABNetwork;
import com.infamous.all_bark_all_bite.common.network.ServerboundAdjustInstrumentPacket;
import com.infamous.all_bark_all_bite.common.network.ServerboundUnbindPetWhistlePacket;
import com.infamous.all_bark_all_bite.common.util.InstrumentUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
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

public class WhistleScreen extends Screen {
    private static final Component CONTAINER_TITLE = Component.translatable(PetWhistleItem.CONTAINER_TITLE_ID);
    private static final Component UNBIND_BUTTON_LABEL = Component.translatable(PetWhistleItem.UNBIND_BUTTON_LABEL_ID);
    private static final ResourceLocation BG_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, "textures/gui/whistle.png");
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
    private static final int UNBIND_BUTTON_WIDTH = 36;
    private static final int UNBIND_BUTTON_HEIGHT = 20;
    private static final int UNBIND_BUTTON_X = 9;
    private static final int UNBIND_BUTTON_Y = 32;
    private final ItemRenderer itemRenderer;
    protected int imageWidth = 176;
    protected int imageHeight = 84;
    private final Player owner;
    private final ItemStack whistle;
    private final InteractionHand hand;
    private final List<Holder<Instrument>> instruments;
    private int selectedInstrumentIndex = -1;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private int leftPos;
    private int topPos;
    private Button unbindButton;

    public WhistleScreen(Player owner, ItemStack whistle, InteractionHand hand, Iterable<Holder<Instrument>> instruments) {
        super(CONTAINER_TITLE);
        this.owner = owner;
        this.whistle = whistle;
        this.hand = hand;
        this.instruments = ImmutableList.copyOf(instruments);
        this.minecraft = Minecraft.getInstance(); // need to do this to prevent NPEs
        this.itemRenderer = this.minecraft.getItemRenderer();
        this.font = this.minecraft.font; // need to do this to prevent NPEs
    }

    @Override
    protected void init() {
        //super.init();
        //this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        /*
        new Button(this.leftPos + UNBIND_BUTTON_X, this.topPos + UNBIND_BUTTON_Y, UNBIND_BUTTON_WIDTH, UNBIND_BUTTON_HEIGHT,
                UNBIND_BUTTON_LABEL, (button) -> {
            this.unbindWhistle();
            this.updateUnbindButton();
        })
         */
        this.unbindButton = this.addRenderableWidget(Button.builder(UNBIND_BUTTON_LABEL, (button) -> {
            this.unbindWhistle();
            this.updateUnbindButton();
        }).bounds(this.leftPos + UNBIND_BUTTON_X, this.topPos + UNBIND_BUTTON_Y, UNBIND_BUTTON_WIDTH, UNBIND_BUTTON_HEIGHT).build());
        this.updateUnbindButton();
    }

    private void updateUnbindButton() {
        this.unbindButton.active = PetWhistleItem.getBoundTo(this.whistle) != null;
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
        //this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, guiGraphics.pose(), mouseX, mouseY);
        this.renderTooltip(guiGraphics, guiGraphics.pose(), mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected void renderBg(GuiGraphics guiGraphics, PoseStack poseStack, int mouseX, int mouseY) {
        this.renderBackground(guiGraphics);
        this.setFocused(null);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(BG_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        int k = (int)(41.0F * this.scrollOffs);
        guiGraphics.blit(BG_LOCATION, i + 119, j + SCROLLER_HEIGHT + k, this.imageWidth + (this.isScrollBarActive() ? 0 : SCROLLER_WIDTH), 0, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        int instrumentsLeftPos = this.leftPos + INSTRUMENTS_X;
        int instrumentsTopPos = this.topPos + INSTRUMENTS_Y;
        int stopIndex = this.startIndex + this.getMaxDisplayButtons();
        this.renderButtons(guiGraphics, poseStack, mouseX, mouseY, instrumentsLeftPos, instrumentsTopPos, stopIndex);
        this.renderInstruments(guiGraphics, poseStack, instrumentsLeftPos, instrumentsTopPos, stopIndex);
    }

    protected void renderTooltip(GuiGraphics guiGraphics, PoseStack poseStack, int mouseX, int mouseY) {
        //super.renderTooltip(poseStack, mouseX, mouseY);
        int instrumentsLeftPos = this.leftPos + INSTRUMENTS_X;
        int instrumentsTopPos = this.topPos + INSTRUMENTS_Y;
        int stopIndex = this.startIndex + this.getMaxDisplayButtons();

        for(int index = this.startIndex; index < stopIndex && index < this.instruments.size(); ++index) {
            int indexDiff = index - this.startIndex;
            int x = instrumentsLeftPos + indexDiff % INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_WIDTH;
            int y = instrumentsTopPos + indexDiff / INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_HEIGHT + 2;
            if (mouseX >= x && mouseX < x + INSTRUMENTS_IMAGE_SIZE_WIDTH && mouseY >= y && mouseY < y + INSTRUMENTS_IMAGE_SIZE_HEIGHT) {
                guiGraphics.renderTooltip(this.font, getInstrumentTooltipLines(this.instruments.get(index)), Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private static List<Component> getInstrumentTooltipLines(Holder<Instrument> instrument) {
        List<Component> tooltipLines = Lists.newArrayList();
        MutableComponent instrumentTooltip = InstrumentUtil.getInstrumentTooltip(instrument);
        tooltipLines.add(instrumentTooltip.withStyle(ChatFormatting.WHITE));
        MutableComponent descriptionTooltip = InstrumentUtil.getInstrumentDescriptionTooltip(instrument);
        tooltipLines.add(descriptionTooltip.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        return tooltipLines;
    }

    private void renderButtons(GuiGraphics guiGraphics, PoseStack poseStack, int mouseX, int mouseY, int instrumentsLeftPos, int instrumentsTopPos, int stopIndex) {
        for(int index = this.startIndex; index < stopIndex && index < this.instruments.size(); ++index) {
            int indexDiff = index - this.startIndex;
            int x = instrumentsLeftPos + indexDiff % INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_WIDTH;
            int columns = indexDiff / INSTRUMENTS_COLUMNS;
            int y = instrumentsTopPos + columns * INSTRUMENTS_IMAGE_SIZE_HEIGHT + 2;
            int buttonY = this.imageHeight;
            if (index == this.selectedInstrumentIndex) {
                buttonY += INSTRUMENTS_IMAGE_SIZE_HEIGHT;
            } else if (mouseX >= x && mouseY >= y && mouseX < x + INSTRUMENTS_IMAGE_SIZE_WIDTH && mouseY < y + INSTRUMENTS_IMAGE_SIZE_HEIGHT) {
                buttonY += INSTRUMENTS_IMAGE_SIZE_HEIGHT * 2;
            }

            guiGraphics.blit(BG_LOCATION, x, y - 1, 0, buttonY, INSTRUMENTS_IMAGE_SIZE_WIDTH, INSTRUMENTS_IMAGE_SIZE_HEIGHT);
        }

    }

    private void renderInstruments(GuiGraphics guiGraphics, PoseStack poseStack, int instrumentsLeftPos, int instrumentsTopPos, int stopIndex) {
        for(int index = this.startIndex; index < stopIndex && index < this.instruments.size(); ++index) {
            int indexDiff = index - this.startIndex;
            int x = instrumentsLeftPos + indexDiff % INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_WIDTH + (INSTRUMENTS_IMAGE_SIZE_WIDTH / 4);
            int columns = indexDiff / INSTRUMENTS_COLUMNS;
            int y = instrumentsTopPos + columns * INSTRUMENTS_IMAGE_SIZE_HEIGHT + 2 + (INSTRUMENTS_IMAGE_SIZE_HEIGHT / 3);

            guiGraphics.drawString(this.font, InstrumentUtil.getInstrumentTooltip(this.instruments.get(index)), x, y, INSTRUMENT_NAME_COLOR);
            //this.minecraft.getItemRenderer().renderAndDecorateItem(this.instruments.get(index).getResultItem(), x, y);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int p_99320_) {
        this.scrolling = false;
        int instrumentsLeftPos = this.leftPos + INSTRUMENTS_X;
        int instrumentsTopPos = this.topPos + INSTRUMENTS_Y;
        int width = this.startIndex + this.getMaxDisplayButtons();

        for(int index = this.startIndex; index < width; ++index) {
            int i1 = index - this.startIndex;
            double adjustedClickX = x - (double)(instrumentsLeftPos + i1 % INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_WIDTH);
            double adjustedClickY = y - (double)(instrumentsTopPos + i1 / INSTRUMENTS_COLUMNS * INSTRUMENTS_IMAGE_SIZE_HEIGHT);
            if (adjustedClickX >= 0.0D && adjustedClickY >= 0.0D && adjustedClickX < INSTRUMENTS_IMAGE_SIZE_WIDTH && adjustedClickY < INSTRUMENTS_IMAGE_SIZE_HEIGHT && this.clickMenuButton(index)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.updateInstrument(this.instruments.get(this.selectedInstrumentIndex));
                //this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, index);
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

            return true;
        }
        return false;
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
        return this.instruments.size() > this.getMaxDisplayButtons();
    }

    private int getMaxDisplayButtons(){
        return INSTRUMENTS_COLUMNS * INSTRUMENTS_ROWS;
    }

    protected int getOffscreenRows() {
        return (this.instruments.size() + INSTRUMENTS_COLUMNS - 1) / Math.max(1, INSTRUMENTS_COLUMNS - INSTRUMENTS_ROWS);
    }

    private void updateInstrument(Holder<Instrument> instrument) {
        InstrumentUtil.setSoundVariantId(this.whistle, instrument);
        ABABNetwork.INSTANCE.sendToServer(new ServerboundAdjustInstrumentPacket(this.getSlot(), InstrumentUtil.getInstrumentLocation(instrument)));
    }

    private int getSlot() {
        return this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().selected : Inventory.SLOT_OFFHAND;
    }

    private void unbindWhistle() {
        PetWhistleItem.unbind(this.whistle);
        ABABNetwork.INSTANCE.sendToServer(new ServerboundUnbindPetWhistlePacket(this.getSlot()));
    }

}
