package com.chumbok.imageservice.dto;

import com.chumbok.imageservice.service.locator.ImageLocator;

import java.util.ListIterator;

public record ImageLocatorChain(ListIterator<? extends ImageLocator> iterator) {}
